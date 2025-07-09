#!/bin/bash

# extract_curl_command.sh
# Extracts curl broadcast command from logcat output

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${PURPLE}🔍 Curl Command Extractor${NC}"
echo -e "${PURPLE}=========================${NC}"
echo ""

# Function to extract curl command from logcat
extract_curl_command() {
    local input_file="$1"
    local output_file="$2"
    
    # Find the start of the curl command
    local start_line=$(grep -n "curl -X POST https://fullnode.mainnet.aptoslabs.com/v1/transactions" "$input_file" | head -1 | cut -d: -f1)
    
    if [[ -z "$start_line" ]]; then
        echo -e "${RED}❌ No curl command found in the input${NC}"
        return 1
    fi
    
    echo -e "${BLUE}📍 Found curl command starting at line: $start_line${NC}"
    
    # Extract the curl command and JSON payload
    local extracting=false
    local curl_command=""
    local json_payload=""
    local in_json=false
    local brace_count=0
    
    while IFS= read -r line; do
        # Remove logcat timestamp and process info
        clean_line=$(echo "$line" | sed 's/^[0-9-]* [0-9:.]* *[0-9]* *[0-9]* D USDTRecovery: //')
        
        # Check if this is the curl command start
        if [[ "$clean_line" == *"curl -X POST"* ]]; then
            extracting=true
            curl_command="$clean_line"
            continue
        fi
        
        # If we're extracting and find the -d parameter, start JSON extraction
        if [[ "$extracting" == true ]] && [[ "$clean_line" == *"-d '"* ]]; then
            in_json=true
            json_payload=$(echo "$clean_line" | sed "s/.*-d '//")
            # Count opening braces in this line
            brace_count=$(echo "$json_payload" | tr -cd '{' | wc -c)
            brace_count=$((brace_count - $(echo "$json_payload" | tr -cd '}' | wc -c)))
            continue
        fi
        
        # If we're in JSON mode, continue collecting
        if [[ "$in_json" == true ]]; then
            # Remove logcat prefix and add to JSON
            if [[ "$clean_line" != *"🚀🚀🚀"* ]] && [[ "$clean_line" != *"💎 READY"* ]]; then
                # Update brace count
                brace_count=$((brace_count + $(echo "$clean_line" | tr -cd '{' | wc -c)))
                brace_count=$((brace_count - $(echo "$clean_line" | tr -cd '}' | wc -c)))
                
                json_payload="$json_payload$clean_line"
                
                # If we've closed all braces and found the ending quote, we're done
                if [[ "$brace_count" -eq 0 ]] && [[ "$clean_line" == *"}"* ]]; then
                    break
                fi
            fi
        fi
        
        # If we're extracting but not in JSON, add to curl command
        if [[ "$extracting" == true ]] && [[ "$in_json" == false ]]; then
            if [[ "$clean_line" == *"-H"* ]] || [[ "$clean_line" == *"\\"* ]]; then
                curl_command="$curl_command $clean_line"
            fi
        fi
        
    done < "$input_file"
    
    # Remove trailing quote and backslash if present
    json_payload=$(echo "$json_payload" | sed "s/'$//" | sed "s/\\\\$//")
    
    # Construct the final curl command
    local final_command="curl -X POST https://fullnode.mainnet.aptoslabs.com/v1/transactions \\
  -H 'Content-Type: application/json' \\
  -d '$json_payload'"
    
    echo "$final_command" > "$output_file"
    echo -e "${GREEN}✅ Curl command extracted successfully${NC}"
    return 0
}

# Function to parse JSON and extract key fields
parse_transaction_details() {
    local json_file="$1"
    
    echo -e "${CYAN}📋 Transaction Details:${NC}"
    
    # Extract key fields using grep and sed
    local sender=$(grep -o '"sender": "[^"]*"' "$json_file" | cut -d'"' -f4)
    local sequence=$(grep -o '"sequence_number": "[^"]*"' "$json_file" | cut -d'"' -f4)
    local gas_amount=$(grep -o '"max_gas_amount": "[^"]*"' "$json_file" | cut -d'"' -f4)
    local gas_price=$(grep -o '"gas_unit_price": "[^"]*"' "$json_file" | cut -d'"' -f4)
    local amount=$(grep -o '"arguments": \[.*\]' "$json_file" | grep -o '"[0-9]*"' | tail -1 | tr -d '"')
    
    echo -e "   💳 Sender: ${sender}"
    echo -e "   🔢 Sequence: ${sequence}"
    echo -e "   ⛽ Gas Limit: ${gas_amount} units"
    echo -e "   💰 Gas Price: ${gas_price} octas"
    
    if [[ -n "$amount" ]]; then
        local usdt_amount=$(echo "scale=6; $amount / 1000000" | bc -l 2>/dev/null || echo "$amount micro-units")
        echo -e "   📊 Amount: ${usdt_amount} USDT"
    fi
    
    local max_cost=$(echo "scale=8; $gas_amount * $gas_price / 100000000" | bc -l 2>/dev/null || echo "~0.001")
    echo -e "   💸 Max Cost: ${max_cost} APT"
    echo ""
}

# Main execution
main() {
    local input_file=""
    local output_file="broadcast_command.sh"
    local temp_json="temp_transaction.json"
    
    # Check if input is provided
    if [[ $# -eq 0 ]]; then
        echo -e "${YELLOW}💡 Usage: $0 <logcat_file> [output_file]${NC}"
        echo -e "${YELLOW}   Or pipe logcat output: cat logcat.txt | $0${NC}"
        echo ""
        
        # Check if input is coming from pipe
        if [[ -t 0 ]]; then
            echo -e "${RED}❌ No input provided${NC}"
            exit 1
        else
            # Read from stdin
            input_file="/tmp/logcat_input.txt"
            cat > "$input_file"
        fi
    else
        input_file="$1"
        if [[ ! -f "$input_file" ]]; then
            echo -e "${RED}❌ Input file not found: $input_file${NC}"
            exit 1
        fi
    fi
    
    # Set output file if provided
    if [[ $# -ge 2 ]]; then
        output_file="$2"
    fi
    
    echo -e "${BLUE}📂 Input file: $input_file${NC}"
    echo -e "${BLUE}📤 Output file: $output_file${NC}"
    echo ""
    
    # Extract curl command
    if extract_curl_command "$input_file" "$output_file"; then
        
        # Extract just the JSON part for analysis
        grep -A 1000 "Content-Type: application/json" "$output_file" | grep -A 1000 "^  -d '" | sed "s/^  -d '//" | sed "s/'$//" > "$temp_json"
        
        # Parse transaction details
        parse_transaction_details "$temp_json"
        
        # Make the output file executable
        chmod +x "$output_file"
        
        echo -e "${GREEN}🎉 SUCCESS! Curl command ready${NC}"
        echo -e "${GREEN}📝 Saved to: $output_file${NC}"
        echo ""
        echo -e "${CYAN}🚀 To execute the transaction:${NC}"
        echo -e "${CYAN}   ./$output_file${NC}"
        echo ""
        echo -e "${YELLOW}⚠️  Make sure you have sufficient APT balance for gas fees${NC}"
        
        # Clean up
        rm -f "$temp_json"
        [[ "$input_file" == "/tmp/logcat_input.txt" ]] && rm -f "$input_file"
        
    else
        echo -e "${RED}❌ Failed to extract curl command${NC}"
        exit 1
    fi
}

# Run main function
main "$@"