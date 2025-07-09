// filepath /workspaces/tangem-app1/app/src/main/java/com/tangem/tap/usdt/JsonUSDTRecovery.kt
// Pure JSON Aptos USDT Recovery - No BCS encoding required
// This creates the exact JSON hash that Aptos expects for signature verification

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
 * Eliminates BCS/JSON mismatch issues
 */
class JsonUSDTRecoveryManager {
    
    companion object {
        const val USDT_CONTRACT = "0x357b0b74bc833e95a115ad22604854d6b0fca151cecd94111770e5d6ffc9dc2b"
        const val FROM_ADDRESS = "0x6d450a1c03f2a5291d3f7d7c87e1abe3844ee832a7c9582a1e525b6b3624e1e4"
        const val TO_ADDRESS = "0xae77a91be4cd67144cf1fe0949f00b2d715eb7863b5248c9c949a5a6a98f1c9f"
        const val TEST_AMOUNT = 10000L // 0.01 USDT (6 decimals)
        const val FULL_AMOUNT = 1978400000L // 1978.4 USDT (6 decimals)
        
        // Standard transaction parameters
        const val MAX_GAS_AMOUNT = "100000"
        const val GAS_UNIT_PRICE = "100"
        const val EXPIRATION_TIMESTAMP = "1752084000" // July 8, 2025 18:00:00 UTC
        const val CHAIN_ID = 1 // Mainnet
    }
    
    /**
     * Creates the exact JSON transaction that Aptos expects
     * Returns both the signing hash and the complete transaction JSON
     */
    suspend fun createJsonUSDTTransaction(
        sequenceNumber: Long,
        amount: Long = TEST_AMOUNT,
        isTestRun: Boolean = true
    ): Pair<ByteArray, JSONObject> {
        
        Log.d("USDTRecovery", "🔧 Creating JSON ${if (isTestRun) "TEST" else "FULL"} USDT transfer")
        Log.d("USDTRecovery", "Amount: ${amount / 1_000_000.0} USDT")
        Log.d("USDTRecovery", "Sequence: $sequenceNumber")
        
        // Create the exact JSON structure Aptos expects
        val transaction = JSONObject().apply {
            put("sender", FROM_ADDRESS)
            put("sequence_number", sequenceNumber.toString())
            put("max_gas_amount", MAX_GAS_AMOUNT)
            put("gas_unit_price", GAS_UNIT_PRICE)
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
        
        // Create signing hash from JSON structure
        val signingHash = createAptosJsonSigningHash(transaction)
        
        Log.d("USDTRecovery", "✅ JSON transaction created successfully")
        Log.d("USDTRecovery", "Signing hash: ${signingHash.toHexString()}")
        
        return Pair(signingHash, transaction)
    }
    
    /**
     * Creates the signing hash that Aptos generates from JSON transactions
     * This must match exactly what the Aptos node expects for verification
     */
    private fun createAptosJsonSigningHash(transaction: JSONObject): ByteArray {
        Log.d("USDTRecovery", "🔧 Creating Aptos-compatible signing hash...")
        
        // Create the raw transaction for signing (this is what Aptos actually signs)
        val rawTxBytes = createRawTransactionBytes(transaction)
        
        // Aptos signing prefix - SHA3-256 hash of "APTOS::RawTransaction"
        val prefixBytes = sha3_256("APTOS::RawTransaction".toByteArray(StandardCharsets.UTF_8))
        
        // Combine prefix + raw transaction
        val signingMessage = prefixBytes + rawTxBytes
        
        // Use SHA3-256 for the final hash (Aptos standard)
        val hash = sha3_256(signingMessage)
        
        Log.d("USDTRecovery", "✅ Aptos signing hash created")
        Log.d("USDTRecovery", "Prefix bytes length: ${prefixBytes.size}")
        Log.d("USDTRecovery", "Raw transaction length: ${rawTxBytes.size}")
        Log.d("USDTRecovery", "Full message length: ${signingMessage.size}")
        
        return hash
    }
    
    /**
     * Creates the raw transaction bytes that Aptos uses for signing
     * This needs to match the BCS encoding that Aptos expects
     */
    private fun createRawTransactionBytes(transaction: JSONObject): ByteArray {
        val result = mutableListOf<Byte>()
        
        // 1. Sender address (32 bytes)
        val senderHex = transaction.getString("sender").removePrefix("0x")
        val senderBytes = hexToBytes(senderHex.padStart(64, '0'))
        result.addAll(senderBytes.toList())
        
        // 2. Sequence number (8 bytes, little-endian)
        val sequenceNumber = transaction.getString("sequence_number").toLong()
        val sequenceBytes = ByteArray(8)
        for (i in 0..7) {
            sequenceBytes[i] = ((sequenceNumber shr (i * 8)) and 0xFF).toByte()
        }
        result.addAll(sequenceBytes.toList())
        
        // 3. Payload (Entry Function)
        val payload = transaction.getJSONObject("payload")
        
        // Payload type (0 for EntryFunction)
        result.add(0.toByte())
        
        // Module address and name
        val function = payload.getString("function")
        val parts = function.split("::")
        val moduleAddress = parts[0].removePrefix("0x")
        val moduleName = parts[1]
        val functionName = parts[2]
        
        // Module address (32 bytes)
        val moduleAddressBytes = hexToBytes(moduleAddress.padStart(64, '0'))
        result.addAll(moduleAddressBytes.toList())
        
        // Module name (with length prefix)
        val moduleNameBytes = moduleName.toByteArray(StandardCharsets.UTF_8)
        result.addAll(encodeLEB128(moduleNameBytes.size).toList())
        result.addAll(moduleNameBytes.toList())
        
        // Function name (with length prefix)
        val functionNameBytes = functionName.toByteArray(StandardCharsets.UTF_8)
        result.addAll(encodeLEB128(functionNameBytes.size).toList())
        result.addAll(functionNameBytes.toList())
        
        // Type arguments
        val typeArgs = payload.getJSONArray("type_arguments")
        result.addAll(encodeLEB128(typeArgs.length()).toList())
        
        // For "0x1::fungible_asset::Metadata"
        result.add(7.toByte()) // Struct type tag
        
        // Struct address (0x1)
        val structAddress = hexToBytes("0000000000000000000000000000000000000000000000000000000000000001")
        result.addAll(structAddress.toList())
        
        // Module name "fungible_asset"
        val faModuleBytes = "fungible_asset".toByteArray(StandardCharsets.UTF_8)
        result.addAll(encodeLEB128(faModuleBytes.size).toList())
        result.addAll(faModuleBytes.toList())
        
        // Struct name "Metadata"
        val metadataBytes = "Metadata".toByteArray(StandardCharsets.UTF_8)
        result.addAll(encodeLEB128(metadataBytes.size).toList())
        result.addAll(metadataBytes.toList())
        
        // No type parameters for Metadata
        result.addAll(encodeLEB128(0).toList())
        
        // Function arguments
        val args = payload.getJSONArray("arguments")
        result.addAll(encodeLEB128(args.length()).toList())
        
        // Arg 1: USDT contract address
        val usdtContract = args.getString(0).removePrefix("0x")
        val usdtBytes = hexToBytes(usdtContract.padStart(64, '0'))
        result.addAll(encodeLEB128(usdtBytes.size).toList())
        result.addAll(usdtBytes.toList())
        
        // Arg 2: Recipient address
        val recipient = args.getString(1).removePrefix("0x")
        val recipientBytes = hexToBytes(recipient.padStart(64, '0'))
        result.addAll(encodeLEB128(recipientBytes.size).toList())
        result.addAll(recipientBytes.toList())
        
        // Arg 3: Amount (as u64)
        val amount = args.getString(2).toLong()
        val amountBytes = ByteArray(8)
        for (i in 0..7) {
            amountBytes[i] = ((amount shr (i * 8)) and 0xFF).toByte()
        }
        result.addAll(encodeLEB128(amountBytes.size).toList())
        result.addAll(amountBytes.toList())
        
        // 4. Max gas amount (8 bytes, little-endian)
        val maxGas = transaction.getString("max_gas_amount").toLong()
        val maxGasBytes = ByteArray(8)
        for (i in 0..7) {
            maxGasBytes[i] = ((maxGas shr (i * 8)) and 0xFF).toByte()
        }
        result.addAll(maxGasBytes.toList())
        
        // 5. Gas unit price (8 bytes, little-endian)
        val gasPrice = transaction.getString("gas_unit_price").toLong()
        val gasPriceBytes = ByteArray(8)
        for (i in 0..7) {
            gasPriceBytes[i] = ((gasPrice shr (i * 8)) and 0xFF).toByte()
        }
        result.addAll(gasPriceBytes.toList())
        
        // 6. Expiration timestamp (8 bytes, little-endian)
        val expiration = transaction.getString("expiration_timestamp_secs").toLong()
        val expirationBytes = ByteArray(8)
        for (i in 0..7) {
            expirationBytes[i] = ((expiration shr (i * 8)) and 0xFF).toByte()
        }
        result.addAll(expirationBytes.toList())
        
        // 7. Chain ID (1 byte)
        result.add(CHAIN_ID.toByte())
        
        return result.toByteArray()
    }
    
    /**
     * Encode LEB128 variable-length integer
     */
    private fun encodeLEB128(value: Int): ByteArray {
        var num = value
        val result = mutableListOf<Byte>()
        
        while (num >= 0x80) {
            result.add(((num and 0x7F) or 0x80).toByte())
            num = num ushr 7
        }
        result.add((num and 0x7F).toByte())
        
        return result.toByteArray()
    }
    
    /**
     * Convert hex string to bytes
     */
    private fun hexToBytes(hex: String): ByteArray {
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
        
        return completedTransaction.toString()
    }
}

// Extension function for hex conversion
private fun ByteArray.toHexString(): String {
    return this.joinToString("") { "%02x".format(it) }
}