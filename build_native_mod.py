#!/usr/bin/env python3
"""
SoundRadar Pro - Standalone Native Builder & Verification Runner
Builds the complete Fabric mod release JAR, runs all unit tests,
and calculates SHA-256 checksums.
"""
import os
import sys
import hashlib
import subprocess
from pathlib import Path

ROOT = Path(__file__).parent.resolve()
LIBS_DIR = ROOT / "build" / "libs"
JAR_NAME = "sound-radar-pro-1.0.0.jar"

def main():
    print("=" * 65)
    print("       SoundRadar Pro - Tactical Acoustic Radar Builder")
    print("=" * 65)

    # 1. Run standalone test suite first for rapid local validation
    print("[*] Running standalone verification test runner...")
    from run_standalone_tests import run_tests
    if not run_tests():
        print("[!] Standalone verification FAILED!")
        return False

    # 2. Execute Gradle build via wrapper
    gradle_cmd = str(ROOT / "gradlew.bat") if os.name == "nt" else "./gradlew"
    print(f"[*] Executing Gradle build via {gradle_cmd}...")

    res = subprocess.run([gradle_cmd, "build", "--info"], cwd=str(ROOT), capture_output=True, text=True)
    if res.returncode != 0:
        print("[!] Gradle build failed:")
        print(res.stdout[-1500:] if len(res.stdout) > 1500 else res.stdout)
        print(res.stderr)
        return False

    print("[+] Gradle build and test suite succeeded!")

    # 3. Verify output jar
    jar_path = LIBS_DIR / JAR_NAME
    if not jar_path.exists():
        print(f"[!] Output jar {jar_path} not found!")
        return False

    jar_size = jar_path.stat().st_size
    print(f"[+] Output JAR: {jar_path} ({jar_size:,} bytes)")

    # 4. Generate SHA-256 Checksum
    h = hashlib.sha256(jar_path.read_bytes()).hexdigest()
    checksum_file = LIBS_DIR / "CHECKSUMS.txt"
    checksum_file.write_text(f"{h}  {JAR_NAME}\n", encoding="utf-8")
    print(f"[+] SHA-256 Checksum: {h}")

    print("=" * 65)
    print(" [SUCCESS] SoundRadar Pro v1.0.0 built & verified 100%!")
    print("=" * 65)
    return True

if __name__ == "__main__":
    if not main():
        sys.exit(1)
    sys.exit(0)
