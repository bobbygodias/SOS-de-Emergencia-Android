#!/usr/bin/env bash
set -euo pipefail
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
: "${SOS_ANDROID_JAR:?Set SOS_ANDROID_JAR to SDK platform android.jar}"
: "${SOS_BUILD_TOOLS:?Set SOS_BUILD_TOOLS to Android build-tools directory}"
: "${SOS_ECJ_JAR:?Set SOS_ECJ_JAR to Eclipse ECJ 3.37.0 jar}"
mkdir -p "$PROJECT_ROOT/build/classes" "$PROJECT_ROOT/build/gen" "$PROJECT_ROOT/build/dex" "$PROJECT_ROOT/build/tests"
rm -f "$PROJECT_ROOT/build/resources.zip" "$PROJECT_ROOT/build/unsigned.apk"
"$SOS_BUILD_TOOLS/aapt2" compile --dir "$PROJECT_ROOT/app/src/main/res" -o "$PROJECT_ROOT/build/resources.zip"
"$SOS_BUILD_TOOLS/aapt2" link -o "$PROJECT_ROOT/build/unsigned.apk" -I "$SOS_ANDROID_JAR" --manifest "$PROJECT_ROOT/app/src/main/AndroidManifest.xml" --java "$PROJECT_ROOT/build/gen" "$PROJECT_ROOT/build/resources.zip"
find "$PROJECT_ROOT/app/src/main/java" "$PROJECT_ROOT/build/gen" -name '*.java' > "$PROJECT_ROOT/build/sources.txt"
java -jar "$SOS_ECJ_JAR" -8 -encoding UTF-8 -warn:none -bootclasspath "$SOS_ANDROID_JAR" -classpath "$SOS_BUILD_TOOLS/core-lambda-stubs.jar" -d "$PROJECT_ROOT/build/classes" @"$PROJECT_ROOT/build/sources.txt"
python3 - "$PROJECT_ROOT" <<'PY'
import pathlib,sys,zipfile
p=pathlib.Path(sys.argv[1])
with zipfile.ZipFile(p/'build/classes.jar','w') as z:
 for f in sorted((p/'build/classes').rglob('*.class')):z.write(f,f.relative_to(p/'build/classes'))
PY
"$SOS_BUILD_TOOLS/d8" --release --min-api 26 --lib "$SOS_ANDROID_JAR" --output "$PROJECT_ROOT/build/dex" "$PROJECT_ROOT/build/classes.jar"
python3 - "$PROJECT_ROOT" <<'PY'
import pathlib,sys,zipfile
p=pathlib.Path(sys.argv[1])
with zipfile.ZipFile(p/'build/unsigned.apk','a') as z:
 for f in sorted((p/'build/dex').glob('*.dex')):z.write(f,f.name)
PY
"$SOS_BUILD_TOOLS/zipalign" -f -p 4 "$PROJECT_ROOT/build/unsigned.apk" "$PROJECT_ROOT/build/aligned.apk"
java -jar "$SOS_ECJ_JAR" -8 -encoding UTF-8 -warn:none -d "$PROJECT_ROOT/build/tests" "$PROJECT_ROOT/app/src/main/java/org/enterprise/sostoque/TapSequence.java" "$PROJECT_ROOT/app/src/main/java/org/enterprise/sostoque/ProximitySequence.java" "$PROJECT_ROOT/app/src/main/java/org/enterprise/sostoque/Numbers.java" "$PROJECT_ROOT/tests/CoreTests.java"
java -cp "$PROJECT_ROOT/build/tests" org.enterprise.sostoque.CoreTests
if [[ -n "${SOS_KEYSTORE:-}" && -n "${SOS_KEYSTORE_PASS_FILE:-}" ]]; then
 "$SOS_BUILD_TOOLS/apksigner" sign --ks "$SOS_KEYSTORE" --ks-key-alias sostoque --ks-pass "file:$SOS_KEYSTORE_PASS_FILE" --out "$PROJECT_ROOT/build/SOS-Toque-1.0.0.apk" "$PROJECT_ROOT/build/aligned.apk"
 "$SOS_BUILD_TOOLS/apksigner" verify --verbose --print-certs "$PROJECT_ROOT/build/SOS-Toque-1.0.0.apk"
else
 echo "Unsigned build created at build/aligned.apk. Configure a private signing key to install."
fi
