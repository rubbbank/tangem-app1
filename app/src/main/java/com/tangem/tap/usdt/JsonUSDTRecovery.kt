// filepath /workspaces/tangem-app1/app/src/main/java/com/tangem/tap/usdt/JsonUSDTRecovery.kt
// Pure JSON Aptos USDT Recovery - With Local BCS Encoding
// This creates the exact signing hash that Aptos expects without API calls

package com.tangem.tap.usdt

import android.util.Log
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import org.json.JSONObject
import org.json.JSONArray

/**
 * Pure JSON-based USDT Recovery Manager
 * Creates signing hash directly from JSON transaction structure
 * Now with local BCS encoding - no API dependency!
 */
class JsonUSDTRecoveryManager {
    
    companion object {
        const val USDT_CONTRACT = "0x357b0b74bc833e95a115ad22604854d6b0fca151cecd94111770e5d6ffc9dc2b"
        const val FROM_ADDRESS = "0x6d450a1c03f2a5291d3f7d7c87e1abe3844ee832a7c9582a1e525b6b3624e1e4"
        const val TO_ADDRESS = "0x1addb1c67283432dacf6648a4f06f4e605dcfc5062160a50c347f78fdcb89705"
        const val TEST_AMOUNT = 10330L // 0.01 USDT (6 decimals)
        const val FULL_AMOUNT = 4321000000L // 1978.4 USDT (6 decimals)
        const val SEQUENCE_NUMBER = 6L // Current sequence number
        
        // UPDATED: Reduced gas parameters based on successful transaction analysis
        const val MAX_GAS_AMOUNT = "10000"  // Reduced from 100000 to 10000 (90% reduction!)
        const val GAS_UNIT_PRICE = "100"    // Keep same as successful transaction
        const val EXPIRATION_TIMESTAMP = "1752124766" // Updated by script
        const val CHAIN_ID = 1 // Mainnet
    }
    
    /**
     * Creates the exact JSON transaction that Aptos expects
     * Returns both the signing hash and the complete transaction JSON
     * 
     * NEW APPROACH: Use local BCS encoding - no API calls needed!
     */
    suspend fun createJsonUSDTTransaction(
        amount: Long = FULL_AMOUNT,
        isTestRun: Boolean = false
    ): Pair<ByteArray, JSONObject> {
        
        Log.d("USDTRecovery", "🔧 Creating JSON ${if (isTestRun) "TEST" else "FULL"} USDT transfer")
        Log.d("USDTRecovery", "Amount: ${amount / 1_000_000.0} USDT")
        Log.d("USDTRecovery", "Sequence: $SEQUENCE_NUMBER")
        Log.d("USDTRecovery", "💰 OPTIMIZED GAS: max_gas=${MAX_GAS_AMOUNT} (was 100000)")
        Log.d("USDTRecovery", "💰 MAX COST: ${(MAX_GAS_AMOUNT.toInt() * GAS_UNIT_PRICE.toInt()) / 100_000_000.0} APT")
        
        // Create the exact JSON structure Aptos expects
        val transaction = JSONObject().apply {
            put("sender", FROM_ADDRESS)
            put("sequence_number", SEQUENCE_NUMBER.toString())
            put("max_gas_amount", MAX_GAS_AMOUNT)        // ← UPDATED: 10000 instead of 100000
            put("gas_unit_price", GAS_UNIT_PRICE)        // ← CONFIRMED: 100 octas
            put("expiration_timestamp_secs", EXPIRATION_TIMESTAMP)
            
            // Payload for fungible asset transfer
            put("payload", JSONObject().apply {
                put("type", "entry_function_payload")
                put("function", "0x1::primary_fungible_store::transfer")
                put("type_arguments", JSONArray().apply {
                    put("0x1::fungible_asset::Metadata")
                })
                put("arguments", JSONArray().apply {
                    put(USDT_CONTRACT)  // Asset metadata object
                    put(TO_ADDRESS)     // Recipient
                    put(amount.toString()) // Amount as string
                })
            })
        }
        
        // Create signing hash using local BCS encoding
        val signingHash = getLocalBCSSigningMessage(transaction)
        
        Log.d("USDTRecovery", "✅ JSON transaction created successfully")
        Log.d("USDTRecovery", "Signing hash: ${signingHash.toHexString()}")
        
        return Pair(signingHash, transaction)
    }
    
    /**
     * Creates JSON transaction with dynamic parameters from ADB command
     * Allows real-time parameter changes without recompiling
     * FIXED: Now inside the class where it can access constants
     */
    suspend fun createJsonUSDTTransactionDynamic(
        receiverAddress: String,
        amount: Long,
        sequenceNumber: Long,
        expirationTimestamp: String
    ): Pair<ByteArray, JSONObject> {
        
        Log.d("USDTRecovery", "🔧 Creating DYNAMIC JSON USDT transfer")
        Log.d("USDTRecovery", "Amount: ${amount / 1_000_000.0} USDT")
        Log.d("USDTRecovery", "Receiver: $receiverAddress")
        Log.d("USDTRecovery", "Sequence: $sequenceNumber")
        Log.d("USDTRecovery", "Expiration: $expirationTimestamp")
        
        // Create the exact JSON structure Aptos expects with dynamic values
        val transaction = JSONObject().apply {
            put("sender", FROM_ADDRESS)
            put("sequence_number", sequenceNumber.toString())
            put("max_gas_amount", MAX_GAS_AMOUNT)
            put("gas_unit_price", GAS_UNIT_PRICE)
            put("expiration_timestamp_secs", expirationTimestamp)
            
            // Payload for fungible asset transfer
            put("payload", JSONObject().apply {
                put("type", "entry_function_payload")
                put("function", "0x1::primary_fungible_store::transfer")
                put("type_arguments", JSONArray().apply {
                    put("0x1::fungible_asset::Metadata")
                })
                put("arguments", JSONArray().apply {
                    put(USDT_CONTRACT)         // Asset metadata object
                    put(receiverAddress)       // Dynamic recipient
                    put(amount.toString())     // Dynamic amount
                })
            })
        }
        
        // Create signing hash using local BCS encoding
        val signingHash = getLocalBCSSigningMessage(transaction)
        
        Log.d("USDTRecovery", "✅ DYNAMIC JSON transaction created successfully")
        Log.d("USDTRecovery", "Signing hash: ${signingHash.toHexString()}")
        
        return Pair(signingHash, transaction)
    }
    
    /**
     * Generate BCS-encoded signing message locally without API calls
     * Based on reverse-engineering actual Aptos encoding patterns
     */
    private suspend fun getLocalBCSSigningMessage(transaction: JSONObject): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("USDTRecovery", "🔧 Generating BCS signing message locally...")
                Log.d("USDTRecovery", "✨ No API calls needed - using local BCS encoding!")
                
                // Extract values from transaction JSON
                val sender = transaction.getString("sender")
                val sequenceNumber = transaction.getString("sequence_number").toLong()
                val maxGasAmount = transaction.getString("max_gas_amount")
                val gasUnitPrice = transaction.getString("gas_unit_price")
                val expirationTimestamp = transaction.getString("expiration_timestamp_secs")
                
                val payload = transaction.getJSONObject("payload")
                val arguments = payload.getJSONArray("arguments")
                val usdtContract = arguments.getString(0)
                val receiverAddress = arguments.getString(1)
                val amount = arguments.getString(2).toLong()
                
                // Generate BCS message locally
                val signingBytes = generateBCSSigningMessage(
                    sender = sender,
                    sequenceNumber = sequenceNumber,
                    maxGasAmount = maxGasAmount,
                    gasUnitPrice = gasUnitPrice,
                    expirationTimestamp = expirationTimestamp,
                    usdtContract = usdtContract,
                    receiverAddress = receiverAddress,
                    amount = amount
                )
                
                Log.d("USDTRecovery", "✅ Local BCS encoding completed!")
                Log.d("USDTRecovery", "🚀 No external API dependency!")
                
                signingBytes
                
            } catch (e: Exception) {
                Log.e("USDTRecovery", "Failed to generate BCS signing message: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Generate BCS-encoded signing message using discovered patterns
     * FIXED: Corrected the arguments structure and encoding
     */
    private fun generateBCSSigningMessage(
        sender: String,
        sequenceNumber: Long,
        maxGasAmount: String,
        gasUnitPrice: String,
        expirationTimestamp: String,
        usdtContract: String,
        receiverAddress: String,
        amount: Long
    ): ByteArray {
        
        Log.d("USDTRecovery", "🔧 Generating BCS signing message with parameters:")
        Log.d("USDTRecovery", "   Sender: $sender")
        Log.d("USDTRecovery", "   Sequence: $sequenceNumber")
        Log.d("USDTRecovery", "   Amount: $amount")
        Log.d("USDTRecovery", "   Gas: $maxGasAmount")
        Log.d("USDTRecovery", "   Expiration: $expirationTimestamp")
        
        // The BCS encoding pattern discovered from analyzing real Aptos responses
        val bcsBuilder = StringBuilder()
        
        // 1. Fixed prefix (32 bytes) - constant for all transactions
        bcsBuilder.append("b5e97db07fa0bd0e5598aa3643a9bc6f6693bddc1a9fec9e674a461eaa00b193")
        
        // 2. Sender address (32 bytes) - remove 0x prefix
        bcsBuilder.append(sender.removePrefix("0x"))
        
        // 3. Sequence number (8 bytes, little-endian)
        bcsBuilder.append(sequenceNumber.toLittleEndianHex(8))
        
        // 4. Fixed transaction type and function identifier
        bcsBuilder.append("020000000000000000000000000000000000000000000000000000000000000001167072696d6172795f66756e6769626c655f73746f7265087472616e73666572010700000000000000000000000000000000000000000000000000000000000000010e66756e6769626c655f6173736574084d65746164617461000320")
        
        // 5. USDT contract address (32 bytes) - remove 0x prefix
        bcsBuilder.append(usdtContract.removePrefix("0x"))
        
        // 6. Receiver address with length prefix
        bcsBuilder.append("20")
        bcsBuilder.append(receiverAddress.removePrefix("0x"))
        
        // 7. Amount with length prefix (FIXED: proper BCS encoding)
        bcsBuilder.append("08")  // Length prefix for amount
        bcsBuilder.append(amount.toLittleEndianHex(8))
        
        // 8. Gas parameters (FIXED: removed extra bytes)
        bcsBuilder.append(maxGasAmount.toLong().toLittleEndianHex(8))
        bcsBuilder.append(gasUnitPrice.toLong().toLittleEndianHex(8))
        
        // 9. Expiration timestamp (8 bytes, little-endian)
        bcsBuilder.append(expirationTimestamp.toLong().toLittleEndianHex(8))
        
        // 10. Fixed suffix
        bcsBuilder.append("01")
        
        val hexString = bcsBuilder.toString()
        Log.d("USDTRecovery", "✅ Generated BCS message: 0x$hexString")
        Log.d("USDTRecovery", "Message length: ${hexString.length} chars (${hexString.length/2} bytes)")
        
        return hexStringToBytes(hexString)
    }
    
    /**
     * Convert Long to little-endian hex string
     */
    private fun Long.toLittleEndianHex(bytes: Int): String {
        val hex = this.toString(16).padStart(bytes * 2, '0')
        return hex.chunked(2).reversed().joinToString("")
    }
    
    /**
     * Convert hex string to byte array
     */
    private fun hexStringToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
    
    /**
     * SHA3-256 implementation for Aptos compatibility
     * Aptos specifically uses SHA3-256, not SHA-256
     */
    private fun sha3_256(input: ByteArray): ByteArray {
        try {
            val digest = MessageDigest.getInstance("SHA3-256")
            return digest.digest(input)
        } catch (e: Exception) {
            Log.w("USDTRecovery", "SHA3-256 not available, falling back to SHA-256")
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(input)
        }
    }
    
    /**
     * Completes the transaction with signature and prepares for broadcast
     */
    fun completeTransactionWithSignature(
        transaction: JSONObject,
        publicKey: ByteArray,
        signature: ByteArray
    ): JSONObject {
        
        Log.d("USDTRecovery", "🔐 Adding signature to transaction")
        Log.d("USDTRecovery", "Public key: ${publicKey.toHexString()}")
        Log.d("USDTRecovery", "Signature: ${signature.toHexString()}")
        
        // Add signature to transaction
        transaction.put("signature", JSONObject().apply {
            put("type", "ed25519_signature")
            put("public_key", "0x${publicKey.toHexString()}")
            put("signature", "0x${signature.toHexString()}")
        })
        
        return transaction
    }
    
    /**
     * Formats the complete transaction for broadcasting
     */
    fun formatForBroadcast(completedTransaction: JSONObject): String {
        Log.d("USDTRecovery", "📡 Formatting transaction for broadcast")
        
        val curlCommand = buildString {
            appendLine("curl -X POST https://fullnode.mainnet.aptoslabs.com/v1/transactions \\")
            appendLine("  -H 'Content-Type: application/json' \\")
            appendLine("  -d '${completedTransaction.toString(2)}'")
        }
        
        Log.d("USDTRecovery", "🚀 BROADCAST COMMAND:")
        Log.d("USDTRecovery", curlCommand)
        Log.d("USDTRecovery", "")
        Log.d("USDTRecovery", "💎 READY TO BROADCAST - COPY THIS CURL COMMAND:")
        Log.d("USDTRecovery", "")
        Log.d("USDTRecovery", curlCommand)
        Log.d("USDTRecovery", "")
        Log.d("USDTRecovery", "🚀🚀🚀 JSON TRANSACTION READY FOR BROADCAST! 🚀🚀🚀")
        
        return completedTransaction.toString()
    }

    // Add this function to JsonUSDTRecoveryManager class:

    /**
    * Automatically fetches the current sequence number from Aptos network
    * No more manual sequence number management!
    */
    private suspend fun getCurrentSequenceNumber(address: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("USDTRecovery", "🔍 Auto-fetching sequence number for: $address")
                
                // Use Aptos REST API to get account info
                val url = "https://fullnode.mainnet.aptoslabs.com/v1/accounts/$address"
                
                // Create a simple HTTP request (using Android's built-in networking)
                val response = makeHttpRequest(url)
                
                // Parse JSON response to extract sequence number
                val accountInfo = JSONObject(response)
                val sequenceNumber = accountInfo.getString("sequence_number").toLong()
                
                Log.d("USDTRecovery", "✅ Auto-fetched sequence number: $sequenceNumber")
                return@withContext sequenceNumber
                
            } catch (e: Exception) {
                Log.e("USDTRecovery", "❌ Failed to auto-fetch sequence: ${e.message}")
                Log.d("USDTRecovery", "📋 Falling back to default sequence: $SEQUENCE_NUMBER")
                
                // Fallback to the constant if network fails
                return@withContext SEQUENCE_NUMBER
            }
        }
    }

    /**
    * Simple HTTP request function
    */
    private suspend fun makeHttpRequest(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000 // 10 seconds
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    throw Exception("HTTP $responseCode")
                }
            } catch (e: Exception) {
                throw Exception("Network request failed: ${e.message}")
            }
        }
    }

    /**
    * Enhanced dynamic transaction creation with auto-sequence
    * NOW WITH AUTOMATIC SEQUENCE NUMBER FETCHING!
    */
    suspend fun createJsonUSDTTransactionDynamicAuto(
        receiverAddress: String,
        amount: Long,
        customSequence: Long? = null,  // Optional - if null, auto-fetch
        expirationTimestamp: String? = null  // Optional - if null, auto-calculate
    ): Pair<ByteArray, JSONObject> {
        
        Log.d("USDTRecovery", "🚀 Creating AUTO-DYNAMIC JSON USDT transfer")
        Log.d("USDTRecovery", "Amount: ${amount / 1_000_000.0} USDT")
        Log.d("USDTRecovery", "Receiver: $receiverAddress")
        
        // Auto-fetch sequence number if not provided
        val sequenceNumber = customSequence ?: getCurrentSequenceNumber(FROM_ADDRESS)
        
        // Auto-calculate expiration if not provided (current time + 12 hours)
        val expiration = expirationTimestamp ?: run {
            val currentTime = System.currentTimeMillis() / 1000
            val expirationTime = currentTime + (12 * 60 * 60) // 12 hours
            expirationTime.toString()
        }
        
        Log.d("USDTRecovery", "✅ Using sequence: $sequenceNumber ${if (customSequence == null) "(auto-fetched)" else "(provided)"}")
        Log.d("USDTRecovery", "✅ Using expiration: $expiration ${if (expirationTimestamp == null) "(auto-calculated)" else "(provided)"}")
        
        // Create the exact JSON structure Aptos expects with dynamic values
        val transaction = JSONObject().apply {
            put("sender", FROM_ADDRESS)
            put("sequence_number", sequenceNumber.toString())
            put("max_gas_amount", MAX_GAS_AMOUNT)
            put("gas_unit_price", GAS_UNIT_PRICE)
            put("expiration_timestamp_secs", expiration)
            
            // Payload for fungible asset transfer
            put("payload", JSONObject().apply {
                put("type", "entry_function_payload")
                put("function", "0x1::primary_fungible_store::transfer")
                put("type_arguments", JSONArray().apply {
                    put("0x1::fungible_asset::Metadata")
                })
                put("arguments", JSONArray().apply {
                    put(USDT_CONTRACT)         // Asset metadata object
                    put(receiverAddress)       // Dynamic recipient
                    put(amount.toString())     // Dynamic amount
                })
            })
        }
        
        // Create signing hash using local BCS encoding
        val signingHash = getLocalBCSSigningMessage(transaction)
        
        Log.d("USDTRecovery", "✅ AUTO-DYNAMIC JSON transaction created successfully")
        Log.d("USDTRecovery", "Signing hash: ${signingHash.toHexString()}")
        
        return Pair(signingHash, transaction)
    }

}

// Extension function for hex conversion
private fun ByteArray.toHexString(): String {
    return this.joinToString("") { "%02x".format(it) }
}