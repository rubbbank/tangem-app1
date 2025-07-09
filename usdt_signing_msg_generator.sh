#!/bin/bash

# usdt_signing_msg_generator.sh
# Aptos USDT Signing Message Generator & Kotlin Code Updater
# This script automates the entire process of generating USDT transactions
# and updating the JsonUSDTRecovery.kt file with fresh signing data

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Constants
USDT_CONTRACT="0x357b0b74bc833e95a115ad22604854d6b0fca151cecd94111770e5d6ffc9dc2b"
APTOS_MAINNET_URL="https://fullnode.mainnet.aptoslabs.com/v1"
MAX_GAS_AMOUNT="10000"
GAS_UNIT_PRICE="100"
KOTLIN_FILE="/workspaces/tangem-app1/app/src/main/java/com/tangem/tap/usdt/JsonUSDTRecovery.kt"

echo -e "${PURPLE}🚀 USDT Signing Message Generator${NC}"
echo -e "${PURPLE}==================================${NC}"
echo ""

# Function to validate Aptos address
validate_address() {
    local address=$1
    # Aptos addresses can be 1-64 hex characters after 0x
    if [[ ! $address =~ ^0x[a-fA-F0-9]{1,64}$ ]]; then
        echo -e "${RED}❌ Invalid Aptos address format. Must be 0x followed by 1-64 hex characters.${NC}"
        return 1
    fi
    return 0
}

# Function to validate USDT amount
validate_amount() {
    local amount=$1
    if ! [[ $amount =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
        echo -e "${RED}❌ Invalid amount format. Use decimal numbers (e.g., 1.5, 100, 0.01).${NC}"
        return 1
    fi
    return 0
}

# Step 1: Get user inputs
echo -e "${CYAN}📝 Step 1: Collecting transaction details${NC}"
echo ""

while true; do
    read -p "🔑 Enter sender wallet address: " SENDER_ADDRESS
    if validate_address "$SENDER_ADDRESS"; then
        break
    fi
done

while true; do
    read -p "🎯 Enter receiver wallet address: " RECEIVER_ADDRESS
    if validate_address "$RECEIVER_ADDRESS"; then
        break
    fi
done

while true; do
    read -p "💰 Enter USDT amount (e.g., 1.5, 100, 0.01): " USDT_AMOUNT
    if validate_amount "$USDT_AMOUNT"; then
        break
    fi
done

# Convert USDT amount to micro units (6 decimals)
# Handle decimal conversion without bc command
if [[ $USDT_AMOUNT == *.* ]]; then
    # Split into integer and decimal parts
    INTEGER_PART=${USDT_AMOUNT%.*}
    DECIMAL_PART=${USDT_AMOUNT#*.}
    
    # Pad decimal part to 6 digits or truncate if longer
    DECIMAL_PART=$(printf "%-6s" "$DECIMAL_PART" | tr ' ' '0')
    DECIMAL_PART=${DECIMAL_PART:0:6}
    
    # Calculate micro units: integer_part * 1000000 + decimal_part
    AMOUNT_MICROS=$((INTEGER_PART * 1000000 + 10#$DECIMAL_PART))
else
    # No decimal part, just multiply by 1000000
    AMOUNT_MICROS=$((USDT_AMOUNT * 1000000))
fi

echo ""
echo -e "${GREEN}✅ Transaction Details:${NC}"
echo -e "   Sender: ${SENDER_ADDRESS}"
echo -e "   Receiver: ${RECEIVER_ADDRESS}"
echo -e "   Amount: ${USDT_AMOUNT} USDT (${AMOUNT_MICROS} micro-units)"
echo ""

# Step 2: Get sequence number
echo -e "${CYAN}📊 Step 2: Getting sequence number for sender${NC}"

SEQUENCE_RESPONSE=$(curl -s "${APTOS_MAINNET_URL}/accounts/${SENDER_ADDRESS}" || echo "ERROR")

if [[ $SEQUENCE_RESPONSE == "ERROR" ]] || [[ $SEQUENCE_RESPONSE == *"error"* ]]; then
    echo -e "${RED}❌ Failed to get account information. Check if the sender address exists.${NC}"
    exit 1
fi

SEQUENCE_NUMBER=$(echo "$SEQUENCE_RESPONSE" | grep -o '"sequence_number":"[0-9]*"' | cut -d'"' -f4)

if [[ -z $SEQUENCE_NUMBER ]]; then
    echo -e "${RED}❌ Could not extract sequence number from response.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Current sequence number: ${SEQUENCE_NUMBER}${NC}"
echo ""

# Step 3: Calculate expiration timestamp (current time + 12 hours)
echo -e "${CYAN}⏰ Step 3: Calculating expiration timestamp${NC}"

CURRENT_TIME=$(date +%s)
EXPIRATION_TIME=$((CURRENT_TIME + 43200))  # Add 12 hours (43200 seconds)

echo -e "${GREEN}✅ Expiration timestamp: ${EXPIRATION_TIME} ($(date -d @${EXPIRATION_TIME}))${NC}"
echo ""

# Step 4: Create transaction payload and get signing data
echo -e "${CYAN}🔐 Step 4: Creating transaction and getting Aptos signing data${NC}"

TRANSACTION_JSON=$(cat <<EOF
{
  "sender": "${SENDER_ADDRESS}",
  "sequence_number": "${SEQUENCE_NUMBER}",
  "max_gas_amount": "${MAX_GAS_AMOUNT}",
  "gas_unit_price": "${GAS_UNIT_PRICE}",
  "expiration_timestamp_secs": "${EXPIRATION_TIME}",
  "payload": {
    "type": "entry_function_payload",
    "function": "0x1::primary_fungible_store::transfer",
    "type_arguments": [
      "0x1::fungible_asset::Metadata"
    ],
    "arguments": [
      "${USDT_CONTRACT}",
      "${RECEIVER_ADDRESS}",
      "${AMOUNT_MICROS}"
    ]
  }
}
EOF
)

echo -e "${BLUE}📤 Sending encode_submission request to Aptos...${NC}"

SIGNING_RESPONSE=$(curl -s -X POST "${APTOS_MAINNET_URL}/transactions/encode_submission" \
  -H 'Content-Type: application/json' \
  -d "$TRANSACTION_JSON")

if [[ $SIGNING_RESPONSE == *"error"* ]] || [[ -z $SIGNING_RESPONSE ]]; then
    echo -e "${RED}❌ Failed to get signing data from Aptos API.${NC}"
    echo -e "${RED}Response: ${SIGNING_RESPONSE}${NC}"
    exit 1
fi

# Clean up the response (remove quotes)
SIGNING_DATA=$(echo "$SIGNING_RESPONSE" | tr -d '"')

echo -e "${GREEN}✅ Received signing data from Aptos API${NC}"
echo -e "${GREEN}✅ Signing data length: ${#SIGNING_DATA} characters${NC}"
echo ""

# Replace Step 5 in your usdt_signing_msg_generator.sh with this corrected version:

# Step 5: Generate sed commands and update Kotlin file
echo -e "${CYAN}🔧 Step 5: Updating JsonUSDTRecovery.kt file${NC}"

# Check if Kotlin file exists
if [[ ! -f "$KOTLIN_FILE" ]]; then
    echo -e "${RED}❌ Kotlin file not found: ${KOTLIN_FILE}${NC}"
    exit 1
fi

# Create backup
BACKUP_FILE="${KOTLIN_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$KOTLIN_FILE" "$BACKUP_FILE"
echo -e "${YELLOW}📁 Backup created: ${BACKUP_FILE}${NC}"

# Generate and apply sed commands to update parameters (CORRECTED NAMES)
echo -e "${BLUE}🔄 Updating FROM_ADDRESS...${NC}"
sed -i "s|const val FROM_ADDRESS = \"0x[^\"]*\"|const val FROM_ADDRESS = \"${SENDER_ADDRESS}\"|g" "$KOTLIN_FILE"

echo -e "${BLUE}🔄 Updating TO_ADDRESS...${NC}"
sed -i "s|const val TO_ADDRESS = \"0x[^\"]*\"|const val TO_ADDRESS = \"${RECEIVER_ADDRESS}\"|g" "$KOTLIN_FILE"

echo -e "${BLUE}🔄 Updating FULL_AMOUNT (not TEST_AMOUNT)...${NC}"
sed -i "s|const val FULL_AMOUNT = [0-9]*L|const val FULL_AMOUNT = ${AMOUNT_MICROS}L|g" "$KOTLIN_FILE"

echo -e "${BLUE}🔄 Updating SEQUENCE_NUMBER...${NC}"
sed -i "s|const val SEQUENCE_NUMBER = [0-9]*L|const val SEQUENCE_NUMBER = ${SEQUENCE_NUMBER}L|g" "$KOTLIN_FILE"

echo -e "${BLUE}🔄 Updating EXPIRATION_TIMESTAMP...${NC}"
sed -i "s|const val EXPIRATION_TIMESTAMP = \"[^\"]*\"|const val EXPIRATION_TIMESTAMP = \"${EXPIRATION_TIME}\"|g" "$KOTLIN_FILE"

echo -e "${GREEN}✅ All parameters updated for local BCS encoding!${NC}"
echo -e "${YELLOW}📝 Note: No hardcoded signing message update needed - using local BCS encoding${NC}"

# Verify updates (CORRECTED VERIFICATION)
echo -e "${CYAN}🔍 Verifying updates...${NC}"
VERIFICATION_PASSED=true

if ! grep -q "$SENDER_ADDRESS" "$KOTLIN_FILE"; then
    echo -e "${RED}❌ Failed to update FROM_ADDRESS${NC}"
    VERIFICATION_PASSED=false
fi

if ! grep -q "$RECEIVER_ADDRESS" "$KOTLIN_FILE"; then
    echo -e "${RED}❌ Failed to update TO_ADDRESS${NC}"
    VERIFICATION_PASSED=false
fi

if ! grep -q "FULL_AMOUNT = ${AMOUNT_MICROS}L" "$KOTLIN_FILE"; then
    echo -e "${RED}❌ Failed to update FULL_AMOUNT${NC}"
    VERIFICATION_PASSED=false
fi

if ! grep -q "SEQUENCE_NUMBER = ${SEQUENCE_NUMBER}L" "$KOTLIN_FILE"; then
    echo -e "${RED}❌ Failed to update SEQUENCE_NUMBER${NC}"
    echo -e "${YELLOW}🔍 Debug: Checking what SEQUENCE_NUMBER values exist in file:${NC}"
    grep "SEQUENCE_NUMBER" "$KOTLIN_FILE" || echo "No SEQUENCE_NUMBER found in file"
    VERIFICATION_PASSED=false
fi

if ! grep -q "$EXPIRATION_TIME" "$KOTLIN_FILE"; then
    echo -e "${RED}❌ Failed to update EXPIRATION_TIMESTAMP${NC}"
    VERIFICATION_PASSED=false
fi

if [[ "$VERIFICATION_PASSED" == true ]]; then
    echo -e "${GREEN}✅ Successfully updated all parameters in JsonUSDTRecovery.kt${NC}"
else
    echo -e "${RED}❌ Some updates failed. Restoring backup...${NC}"
    cp "$BACKUP_FILE" "$KOTLIN_FILE"
    exit 1
fi

echo ""

# Step 6: Display summary and next steps
echo -e "${PURPLE}🎉 TRANSACTION READY!${NC}"
echo -e "${PURPLE}===================${NC}"
echo ""
echo -e "${GREEN}📋 Transaction Summary:${NC}"
echo -e "   Sender: ${SENDER_ADDRESS}"
echo -e "   Receiver: ${RECEIVER_ADDRESS}"
echo -e "   Amount: ${USDT_AMOUNT} USDT"
echo -e "   Sequence: ${SEQUENCE_NUMBER}"
echo -e "   Max Gas: ${MAX_GAS_AMOUNT} units"
echo -e "   Expires: $(date -d @${EXPIRATION_TIME})"
echo -e "   Epoch: ${EXPIRATION_TIME}"
echo ""
echo -e "${YELLOW}📱 Next Steps:${NC}"
echo -e "   1. Rebuild the Android app:"
echo -e "      ${CYAN}cd /workspaces/tangem-app1${NC}"
echo -e "      ${CYAN}./gradlew assembleDebug${NC}"
echo -e "      ${CYAN}adb install -r app/build/outputs/apk/debug/app-debug.apk${NC}"
echo ""
echo -e "   2. Trigger the USDT recovery:"
echo -e "      ${CYAN}adb shell am broadcast -a com.tangem.USDT_RESCUE_TRIGGER${NC}"
echo ""
echo -e "   3. Scan your Tangem card when prompted"
echo ""
echo -e "   4. Execute the generated curl command"
echo ""
echo -e "${GREEN}🎯 Expected Transaction Cost: ~0.000015 APT${NC}"
echo -e "${GREEN}🎯 Maximum Transaction Cost: 0.001 APT${NC}"
echo ""

# Optional: Display the sed commands for reference
echo -e "${BLUE}📝 Generated sed commands (for reference):${NC}"
echo -e "${CYAN}sed -i 's|const val FROM_ADDRESS = \"0x[^\"]*\"|const val FROM_ADDRESS = \"${SENDER_ADDRESS}\"|g' ${KOTLIN_FILE}${NC}"
echo -e "${CYAN}sed -i 's|const val TO_ADDRESS = \"0x[^\"]*\"|const val TO_ADDRESS = \"${RECEIVER_ADDRESS}\"|g' ${KOTLIN_FILE}${NC}"
echo -e "${CYAN}sed -i 's|const val TEST_AMOUNT = [0-9]*L|const val TEST_AMOUNT = ${AMOUNT_MICROS}L|g' ${KOTLIN_FILE}${NC}"
echo -e "${CYAN}sed -i 's|const val SEQUENCE_NUMBER = [0-9]*L|const val SEQUENCE_NUMBER = ${SEQUENCE_NUMBER}L|g' ${KOTLIN_FILE}${NC}"
echo -e "${CYAN}sed -i 's|const val EXPIRATION_TIMESTAMP = \"[^\"]*\"|const val EXPIRATION_TIMESTAMP = \"${EXPIRATION_TIME}\"|g' ${KOTLIN_FILE}${NC}"
echo -e "${CYAN}sed -i 's|val aptosResponse = \"0x[^\"]*\"|val aptosResponse = \"${SIGNING_DATA}\"|g' ${KOTLIN_FILE}${NC}"
echo ""

echo -e "${PURPLE}🚀 Ready to execute your USDT transaction!${NC}"