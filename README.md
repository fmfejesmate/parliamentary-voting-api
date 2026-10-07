# parliamentary-voting-api

Parlamenti szavazó REST API alkalmazás (Java) (Országgyűlés Hivatal Java fejlesztő pozícióhoz tesztfeladat)

## Technikai követelmények

**Konténeres futtatáshoz**:

- Docker (az alkalmazás Docker-konténerben fut, a H2 adatbázissal együtt)

**Közvetlen futtatáshoz**:

- Java 21
- Docker

## Alkalmazás indítása

### Közvetlen lokális indítás

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

**Főbb taskok:**

`test` - fordít, futtatja a teszteket;
`build` - fordít, futtatja a teszteket, elkészíti az alkalmazás generált JAR fájlként;
`bootRun` - fordít, és indítja az alkalmazásszervert és az alkalmazást;
`dockerBuildImage` - elkészíti a JAR-t, majd Docker image-et épít belőle (`parliamentary-voting-api:0.0.1-SNAPSHOT`);

(Leállítás: Ctrl + C) 

***A test / build / bootRun futtatásához JDK 21 kell (JAVA_HOME vagy PATH)!***

***Az alkalmazást két módon profile-al lehet indítani, `default` vagy `local`:***
***default: `.\gradlew.bat bootRun`***
***local: `.\gradlew.bat bootRun --args="--spring.profiles.active=local"`***
***`local` esetben a Swagger és a H2 konzol elérhető, máskülönben ki van kapcsolva!***

### Docker indítás

Futó Docker daemon kell hozzá.
Két lépésből áll a folyamat:
1. Image építése: `.\gradlew.bat dockerBuildImage`
2. a) Elkészült image indítása: `docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT`
2. b) `local` profillal való indítás: `docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT --spring.profiles.active=local`

A H2 a konténeren belül, memóriában fut. A konténer leállításakor az adatok elvesznek.

### Alapadatok

Az alkalmazás indulásakor automatikusan létrejönnek adatok a DB-ben, hogy könnyebb legyen tesztelni azt.

Az alábbi adatok jönnek létre:

*Szavazás entitások:*
*ID          Típus           Időpont                 Elnök*
*TE1         jelenlét (j)    2023-09-28T11:06:25Z    Kepviselo1 + 3 szavazat (Kepviselo1-3)*
*TE2         egyszerű (e)    2023-09-28T14:30:00Z    Kepviselo1 + 3 szavazat (Kepviselo1-3)*

## Swagger elérhetősége

http://localhost:8080/swagger-ui.html
(csak `local` profillal való indulás esetén elérhető!)

## H2 konzol elérhetősége

http://localhost:8080/h2-console
(csak `local` profillal való indulás esetén elérhető!)

JDBC URL: jdbc:h2:mem:szavazasok, user: sa, jelszó üres

***#INFO: A feladat nem határozza meg egyértelműen, hogy in-memory megoldást vár el vagy fájlos tárolást, csak úgy fogalmaz, hogy a futó alkalmazás elmentse és vissza tudja olvasni a szavazást. Ezért az alkalmazás jelenleg in-memory megoldással működik, amely eleget tesz ennek a feltételnek. Azaz újraindítás után az adatok elvesznek.***
