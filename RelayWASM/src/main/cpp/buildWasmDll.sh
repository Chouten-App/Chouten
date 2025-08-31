set -e

cd "./src/main/cpp"

LIB_DIR="$HOME/RelayWASMLibs"
mkdir -p "$LIB_DIR"

# DLL output path
DLL_OUTPUT="$LIB_DIR/RelayWASM.dll"
echo "Current directory: $(pwd)"
echo "[Step 1] Compiling wasm3 C sources..."
for f in ./wasm3/source/*.c; do
    x86_64-w64-mingw32-gcc -I./wasm3/source -c "$f" -o "${f%.c}.o"
done
echo "[Step 2] Compiling RelayWASM.cpp..."
x86_64-w64-mingw32-g++ \
    -I"/mnt/c/Program Files/Eclipse Adoptium/jdk-21.0.7.6-hotspot/include" \
    -I"/mnt/c/Program Files/Eclipse Adoptium/jdk-21.0.7.6-hotspot/include/win32" \
    -I"./wasm3/source" \
    -c ./RelayWASM.cpp -o ./RelayWASM.o \
    -static-libgcc -static-libstdc++
echo "[Step 3] Linking into RelayWASM.dll..."
x86_64-w64-mingw32-g++ -shared -o "$DLL_OUTPUT" ./RelayWASM.o ./wasm3/source/*.o \
    -static-libgcc -static-libstdc++
echo "[Step 4] Move Dll from WSL home dir to Windows home dir"
mv "$DLL_OUTPUT" "$(wslpath "$(cmd.exe /C "echo %USERPROFILE%" | tr -d '\r')")/RelayWASMLibs/"