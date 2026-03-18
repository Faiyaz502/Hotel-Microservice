-- KEYS[1..N] = hold keys
-- ARGV[1..N] = available capacity
-- ARGV[N+1] = TTL
-- ARGV[N+2] = idempotency key
-- ARGV[N+3] = payment token
-- ARGV[N+4] = token key

local num_dates = #KEYS
local ttl = tonumber(ARGV[num_dates + 1])
local idemp_key = ARGV[num_dates + 2]
local pay_token = ARGV[num_dates + 3]
local token_key = ARGV[num_dates + 4]

-- 1. Idempotency
local existing = redis.call("GET", idemp_key)
if existing then return existing end

-- 2. Check availability
for i = 1, num_dates do
	local holds = tonumber(redis.call("GET", KEYS[i]) or "0")
	local capacity = tonumber(ARGV[i])
	if holds >= capacity then
		return "SOLD_OUT"
	end
end

-- 3. Increment holds
for i = 1, num_dates do
	local new_val = redis.call("INCR", KEYS[i])
	if new_val == 1 then
		redis.call("EXPIRE", KEYS[i], ttl)
	end
end

-- 4. Store token (CRITICAL)
redis.call("SET", token_key, "HELD", "EX", ttl)

-- 5. Store idempotency
redis.call("SET", idemp_key, pay_token, "EX", ttl)

return pay_token