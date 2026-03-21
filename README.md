# RogueForge
2D Pixel Art Game

## Run

macOS / Linux:
`./run.sh`

Windows:
`run.bat`

Both scripts check for Java, make sure the Gradle wrapper is available, and launch the desktop build with `:lwjgl3:run`.

## Package Installers

macOS `.dmg`:
`./gradlew :lwjgl3:packageDmg`

Windows `.exe`:
`gradlew.bat :lwjgl3:packageExe`

Both packaging tasks output installers into `lwjgl3/build/package` and require `jpackage` from a JDK that includes it.
