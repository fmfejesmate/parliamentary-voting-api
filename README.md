# parliamentary-voting-api

Parlamenti szavazó REST API alkalmazás (Java) (Országgyűlés Hivatal Java fejlesztő pozícióhoz tesztfeladat)

## Technikai követelmények

**Futtatáshoz**:

- Docker (az alkalmazás Docker-konténerben fut, a H2 adatbázissal együtt)

**Buildhez**:

- Java 21
- Docker

## Build folyamat

A Gradle-nek nem szükséges telepítve lennie a gépre külön, mert a wrapper script letölti: Windowson `gradlew.bat`, Linuxon/macOS-en `gradlew`. Az alábbi parancsokkal tudjuk a scriptet futtatni a különböző operaciós rendszereken:

Windows: `.\gradlew.bat test`
Linux / macOS: `./gradlew test`

Folyamat lépései:
1. Elindítjuk a wrapper scriptet, és átadod a task nevét (pl: `test`).
2. A script a `gradle/wrapper/gradle-wrapper.properties` alapján tudja, melyik Gradle-verzió kell (jelenleg 9.7.1).
3. Ha ez a verzió még nincs a gépen, a `gradle/wrapper/gradle-wrapper.jar` letölti és cache-eli (általában a user könyvtár alatt, `GRADLE_USER_HOME`).
4. A letöltött Gradle olvassa a `settings.gradle` és `build.gradle` fájlokat, majd futtatja a kért taskot (pl `test`).

A teljes task lista: `.\gradlew.bat tasks`
Egy task magyarázata: `.\gradlew.bat help --task test`

#TODO: a teljes alkalmazás buildje még kiegészítésre vár!

## Swagger elérhetősége

http://localhost:8080/swagger-ui.html

## H2 konzol elérhetősége

http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:szavazasok, user: sa, jelszó üres

A feladat nem határozza meg egyértelműen, hogy in-memory megoldást vár el vagy fájlos tárolást, csak úgy fogalmaz, hogy a futó alkalmazás elmentse és vissza tudja olvasni a szavazást. Ezért az alkalmazás jelenleg in-memory megoldással működik, amely eleget tesz ennek a feltételnek. Azaz újraindítás után az adatok elvesznek. 
