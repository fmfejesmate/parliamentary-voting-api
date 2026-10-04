# parliamentary-voting-api

Parlamenti szavazó REST API alkalmazás (Java) (Országgyűlés Hivatal Java fejlesztő pozícióhoz tesztfeladat)

## Technikai követelmények

**Futtatáshoz**:

- Docker (az alkalmazás Docker-konténerben fut, a H2 adatbázissal együtt)

**Buildhez**:

- Java 21
- Docker

## Build folyamat

A Gradle nincs a PATH-on. A belépő a wrapper script: Windowson `gradlew.bat`, Linuxon/macOS-en `gradlew`. Az alábbi parancsokkal tudjuk a scriptet futtatni a különböző operaciós rendszereken:

Windows: `.\gradlew.bat test`
Linux / macOS: `./gradlew test`

1. Te elindítod a wrapper scriptet, és átadod a task nevét (itt: `test`).
2. A script a `gradle/wrapper/gradle-wrapper.properties` alapján tudja, melyik Gradle-verzió kell (jelenleg 9.7.1).
3. Ha ez a verzió még nincs a gépen, a `gradle/wrapper/gradle-wrapper.jar` letölti és cache-eli (általában a user könyvtár alatt, `GRADLE_USER_HOME`).
4. A letöltött Gradle olvassa a `settings.gradle` és `build.gradle` fájlokat, majd futtatja a kért taskot (pl `test`).
