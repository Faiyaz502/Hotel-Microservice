-- KEYS[1..N] = hold keys for each date
-- ARGV[1..N] = available capacity for each date
-- ARGV[N+1] = TTL in seconds
-- ARGV[N+2] = idempotency key
-- ARGV[N+3] = payment token

local num_dates = #KEYS
local ttl = tonumber(ARGV[num_dates + 1])
local idemp_key = ARGV[num_dates + 2]
local pay_token = ARGV[num_dates + 3]

-- 1️⃣ Check idempotency
local existing = redis.call("GET", idemp_key)
if existing then return existing end

-- 2️⃣ Validate availability
for i = 1, num_dates do
	local current_holds = tonumber(redis.call("GET", KEYS[i]) or "0")
	local max_available = tonumber(ARGV[i])
	if current_holds >= max_available then
		return "SOLD_OUT"
	end
end

-- 3️⃣ Atomically increment holds (only set EXPIRE if first hold)
for i = 1, num_dates do
	local new_val = redis.call("INCR", KEYS[i])
	if new_val == 1 then
		redis.call("EXPIRE", KEYS[i], ttl)
	end
end

-- 4️⃣ Set idempotency key
redis.call("SET", idemp_key, pay_token, "EX", ttl)
return pay_token