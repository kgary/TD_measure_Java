# GIFT Backup Script - README

## Overview

This script creates automated backups of your GIFT project's `data` and `output` folders. It packages both folders into a single timestamped ZIP file with a README containing backup information.

## What You Get

When you run this script, it creates:
- **One ZIP file** with timestamp: `GIFT_backup_2025-10-29_12-33-48.zip`
- **Inside the ZIP:**
  - `README.txt` - Information about the backup (device name, source location, timestamp)
  - `data/` folder - All your data files
  - `output/` folder - All your output files

## Prerequisites

### Required Software
1. **Java** - Already installed if you're running GIFT
2. **Apache Ant** - Check if installed by running:
   ```bash
   ant -version
   ```
   If not installed, download from: https://ant.apache.org/bindownload.cgi

### What You Need to Know
- Location of your GIFT project folder
- Location where you want to save backups

## Setup Instructions

### Step 1: Download the Script

# Clone the repo
git clone https://github.com/kgary/TD_measure_Java.git

# Switch to branch
cd TD_measure_Java
git checkout 47-data-backup-to-external-pen-drive

# Follow the README instructions to configure and run

cd \TD_measure_Java\backup-script -> the `backup-build.xml` file is present at this location in the repo.

For example:
- `C:\TD_measure_Java\backup-script\backup-build.xml`

### Step 2: Edit Configuration

Open `backup-build.xml` in any text editor (Notepad, VS Code, etc.) and update **TWO properties** near the top (around lines 11-12):

```xml
<property name="gift.base.dir" location="C:/GIFT-ASU-EXP/GIFT-ASU/GIFT"/>
<property name="backup.destination.dir" location="C:/GIFT-ASU-EXP/Gift-backup"/>
```

**Change these to YOUR paths:**

1. **gift.base.dir** - The full path to your GIFT folder (the one containing `data` and `output` folders)
   
   Example paths:
   - Windows: `C:/GIFT-ASU-EXP/GIFT-ASU/GIFT`
   - Mac: `/Users/yourname/Desktop/GIFT/GIFT-ASU/GIFT`
   - Linux: `/home/yourname/GIFT/GIFT-ASU/GIFT`

2. **backup.destination.dir** - Where you want the backup ZIP files saved
   
   Example paths:
   - Windows: `C:/GIFT-ASU-EXP/Gift-backup`
   - Mac: `/Users/yourname/Desktop/GIFT-Backups`
   - Linux: `/home/yourname/backups`

**IMPORTANT:** Use forward slashes `/` even on Windows! Ant handles them correctly.

### Step 3: Verify Your Paths

Make sure:
- The GIFT folder path exists and contains `data` and `output` folders
- The backup destination folder exists (or the script will create it)
- You have write permissions to the backup destination

## How to Run the Script

### Option 1: From Command Line / Terminal / Git Bash

Navigate to where you saved `backup-build.xml`:

```bash
cd C:/GIFT-ASU-EXP/Gift-backup
```

Run the script:

```bash
ant -f backup-build.xml
```

### Option 2: From Anywhere

You can run it from any location by providing the full path:

```bash
ant -f C:/GIFT-ASU-EXP/Gift-backup/backup-build.xml
```

### Option 3: Create a Shell Script (Mac/Linux)

Create a file `run-backup.sh`:

```bash
#!/bin/bash
ant -f backup-build.xml
```

Make it executable:
```bash
chmod +x run-backup.sh
```

Run it:
```bash
./run-backup.sh
```

## What Happens When You Run It

You'll see output like this:

```
========================================
GIFT Data Backup Script
========================================
Timestamp: 2025-10-29_12-33-48
Device: EN4119342W
Source: C:\GIFT-ASU-EXP\GIFT-ASU\GIFT
Destination: C:\GIFT-ASU-EXP\Gift-backup
========================================

Preparing backup folders...
  Copying 'data' folder...
  Copying 'output' folder...
✓ Folders staged successfully

Generating README file...
✓ README.txt generated successfully

Creating backup archive...
✓ Successfully created backup archive
  File: GIFT_backup_2025-10-29_12-33-48.zip
  Size: 45678901 bytes

Cleaning up temporary files...
✓ Cleanup complete

========================================
Backup Complete!
========================================
Backup location: C:\GIFT-ASU-EXP\Gift-backup
Backup file: GIFT_backup_2025-10-29_12-33-48.zip

The zip file contains:
  - README.txt (backup information)
  - data/
  - output/

You can now copy the backup file to your pendrive.
```

## After Backup Completes

1. Go to your backup destination folder
2. Find the ZIP file: `GIFT_backup_2025-10-29_12-33-48.zip`
3. Copy it to your pendrive or external storage or update the `backup.destination.dir` to the file location of pendrive->this would directly create the backup in the external drive
4. Keep multiple backups in different locations for safety!

## Troubleshooting

### Error: "ant: command not found"

**Problem:** Ant is not installed or not in your PATH.

**Solution:** 
1. Download Ant from: https://ant.apache.org/bindownload.cgi
2. Extract it and add the `bin` folder to your system PATH
3. Restart your terminal/command prompt

### Error: "data folder not found" or "output folder not found"

**Problem:** The `gift.base.dir` path is incorrect.

**Solution:**
1. Open `backup-build.xml`
2. Check line 11: `<property name="gift.base.dir" location="..."/>`
3. Make sure the path points to the GIFT folder that contains `data` and `output`
4. Use forward slashes `/` even on Windows

### Error: Permission denied

**Problem:** You don't have write permissions to the backup destination.

**Solution:**
1. Choose a different backup destination folder
2. Or run the command with administrator/sudo privileges
3. Make sure the folder isn't read-only

### The script runs but creates backup in wrong location

**Problem:** The `backup.destination.dir` path is incorrect.

**Solution:**
1. Open `backup-build.xml`
2. Check line 12: `<property name="backup.destination.dir" location="..."/>`
3. Update to your desired backup location
4. Use forward slashes `/`


## Tips for Regular Backups

### For Live Testing (1 Month)

Run the backup script:
- **Daily:** At the end of each run each day

### Organizing Backups

Create folders by date:
```
Gift-backup/
├── 2025-10-29/
│   └── GIFT_backup_2025-10-29_12-33-48.zip
├── 2025-10-30/
│   └── GIFT_backup_2025-10-30_09-15-22.zip
└── 2025-10-31/
    └── GIFT_backup_2025-10-31_16-45-10.zip
```

## Restoring from Backup

If you need to restore your data:

1. Extract the ZIP file
2. Open `README.txt` to verify source information
3. Copy `data/` folder to your GIFT installation
4. Copy `output/` folder to your GIFT installation

**WARNING:** Always backup existing data before restoring!

## Support

If you encounter issues:
1. Check the Troubleshooting section above
2. Verify your paths in the XML file
3. Make sure you have read/write permissions
4. Check that `data` and `output` folders exist in your GIFT directory

## Script Information

- **Purpose:** Backup GIFT project data and output folders
- **Technology:** Apache Ant build script
- **Cross-platform:** Works on Windows, Mac, and Linux
- **Compression:** Maximum compression (level 9)
- **Output:** Single timestamped ZIP file with README

**Last Updated:** November 04, 2025
**Version:** 1.0
