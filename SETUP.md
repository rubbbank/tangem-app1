# Development Setup Guide

## Required Configuration Files

### Firebase Configuration (google-services.json)
This file contains sensitive API keys and should NEVER be committed to git.

**Setup Steps:**
1. Download google-services.json from Firebase Console
2. Place in: `app/google-services.json`
3. Verify it's in .gitignore

**Required Values:**
- project_id: Your Firebase project ID
- api_key: Your Firebase API key
- project_number: Your Firebase project number

### Security Checklist
- [ ] google-services.json is in .gitignore
- [ ] No API keys committed to git
- [ ] Configuration templates documented
