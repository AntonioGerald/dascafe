# Checkout Error Troubleshooting Guide

## Changes Made

### 1. Shortened Cart Notifications ✅
- "Product name added to cart" → "Added"
- "Product name removed from cart" → "Removed"
- Much faster and less intrusive

### 2. Enhanced Error Logging ✅
- Added detailed logging for order creation
- Shows exact error code, message, and response body
- Logs request data (user_id, total, payment_method)

## How to Debug the Checkout Error

### Step 1: Check Logcat
1. Open Android Studio
2. Go to Logcat (bottom panel)
3. Filter by "ORDER"
4. Try to checkout
5. Look for these log messages:
   - `ORDER_REQUEST` - Shows what data is being sent
   - `ORDER_ERROR` - Shows the exact error from Supabase
   - `ORDER_SUCCESS` - Confirms order was created

### Step 2: Common Supabase Issues

#### Issue 1: Table Permissions
Your `orders` table might not allow INSERT operations.

**Fix in Supabase:**
1. Go to Supabase Dashboard
2. Click on "Authentication" → "Policies"
3. Find the `orders` table
4. Add a policy to allow INSERT:
   ```sql
   CREATE POLICY "Allow insert for authenticated users"
   ON orders FOR INSERT
   TO authenticated, anon
   WITH CHECK (true);
   ```

#### Issue 2: Missing Columns
The `orders` table might be missing required columns.

**Required columns:**
- `id` (int, primary key, auto-increment)
- `user_id` (int)
- `total_amount` (numeric/decimal)
- `payment_method` (text/varchar)
- `status` (text/varchar)
- `created_at` (timestamp, optional but recommended)

**Check in Supabase:**
1. Go to Table Editor
2. Select `orders` table
3. Verify all columns exist with correct types

#### Issue 3: Foreign Key Constraints
If `user_id` has a foreign key to `users` table, make sure the user exists.

**Check:**
1. Go to Table Editor → `users`
2. Verify your user_id exists in the users table
3. The app logs will show: `Creating order - UserID: X`
4. Make sure that UserID X exists in your users table

#### Issue 4: Default Values
Some columns might require default values.

**Common fixes:**
- `created_at` should have default: `now()`
- `status` could have default: `'pending'`

### Step 3: Test Manually in Supabase

Try inserting an order manually:
1. Go to Supabase → Table Editor → `orders`
2. Click "Insert row"
3. Fill in:
   - user_id: (your test user ID)
   - total_amount: 50000
   - payment_method: "cash"
   - status: "pending"
4. Click Save

If this fails, you'll see the exact error from Supabase.

### Step 4: Check API Response

The error message will now show:
- **"Failed: Unauthorized (401)"** → API key issue or RLS policy
- **"Failed: Bad Request (400)"** → Missing/invalid fields
- **"Failed: Not Found (404)"** → Table doesn't exist or wrong endpoint
- **"Order created but no ID returned"** → Insert succeeded but `Prefer: return=representation` not working

## Expected Logcat Output (Success)

```
D/ORDER_REQUEST: Creating order - UserID: 1, Total: 50000.0, Payment: Cash
D/ORDER_SUCCESS: Order created with ID: 123
```

## Expected Logcat Output (Error)

```
D/ORDER_REQUEST: Creating order - UserID: 1, Total: 50000.0, Payment: Cash
E/ORDER_ERROR: Code: 401 | Message: Unauthorized | Body: {"message":"..."}
```

## Quick Fix Checklist

- [ ] Check Logcat for exact error code
- [ ] Verify `orders` table exists in Supabase
- [ ] Check RLS policies allow INSERT
- [ ] Verify all required columns exist
- [ ] Confirm user_id exists in users table
- [ ] Test manual insert in Supabase
- [ ] Check API keys are correct in ApiService.java

## Need More Help?

Share the Logcat output (the `ORDER_ERROR` line) and I can provide a specific fix!
