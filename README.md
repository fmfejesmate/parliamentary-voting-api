# parliamentary-voting-api

Parlamenti szavazó REST API alkalmazás (Java) (Országgyűlés Hivatal Java fejlesztő pozícióhoz tesztfeladat)

## Technikai követelmények

**Konténeres futtatáshoz**:

- Java 21
- Docker (az alkalmazás Docker-konténerben fut, a H2 adatbázissal együtt)

**Közvetlen futtatáshoz**:

- Java 21

## Alkalmazás indítása

**- Az egyes parancsokat az alkalmazás klónozott mappájába navigálva szükséges kiadni!**
**- Az alkalmazás indítása automatizálva van egy scriptes indításra!**
**- Az alkalmazást profile-al vagy anélkül is lehet indítani ( `local` profile).**
**- `local` esetben a Swagger és a H2 konzol elérhető, máskülönben ki van kapcsolva!**
**- Előbbi esetben a Swagger és a H2 konzol az alapértelmezett böngészőben megnyílik automatikusan, amint az alkalmazás elérhető.**
**- A H2 a memóriában fut. Az alkalmazás leállításakor az adatok elvesznek.**
**- Leállítás: `Ctrl` + `C`**


### Közvetlen lokális indítás

**Az alább vázolt parancsok futtatásához JDK 21 kell (JAVA_HOME vagy PATH)!**

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

**Indítás:**

1. profil nélkül: `.\script\boot-run.bat`
2. `local`: `.\script\boot-run-local.bat`
(Linuxon/macOS-en: `./script/boot-run.sh` és `./script/boot-run-local.sh`)

### Docker indítás

**Futó Docker daemon és JDK 21 (JAVA_HOME vagy PATH) kell hozzá!**
A script felépíti az image-et, majd elindítja a konténert.

1. profil nélkül: `.\script\docker-run.bat`
2. `local` profil: `.\script\docker-run-local.bat` 
(Linuxon/macOS-en: `./script/docker-run.sh` és `./script/docker-run-local.sh`)

### Alapadatok

Az alkalmazás indulásakor automatikusan létrejönnek adatok a DB-ben, hogy könnyebb legyen tesztelni.

Példák üres adatbázis után:

- `GET /szavazasok/szavazat?szavazas=TE1&kepviselo=Kepviselo2` → `n`
- `GET /szavazasok/eredmeny?szavazas=TE0` → `U`, `kepviselokSzama` 0
- `GET /szavazasok/eredmeny?szavazas=TE8` → `U`, `kepviselokSzama` 200
- `GET /szavazasok/napi-szavazasok?nap=2023-09-28` → TE1–TE8
- `GET /szavazasok/napi-szavazasok?nap=2020-01-01` → üres lista
- `GET /szavazasok/kepviselo-reszvetel-atlag?kezdet=2023-09-28&veg=2023-09-28` → `5.33`
- `GET /szavazasok/kulonleges-eljarasok-szama?kezdet=2023-09-28&veg=2023-09-28` → `s`/`k`/`e` mind `F` 1 és `U` 1, összesen 6

## Swagger elérhetősége

http://localhost:8080/swagger-ui.html
(csak `local` profillal való indulás esetén elérhető!)

## H2 konzol elérhetősége

http://localhost:8080/h2-console
(csak `local` profillal való indulás esetén elérhető!)

JDBC URL: jdbc:h2:mem:szavazasok, user: sa, jelszó üres

***#INFO: A feladat nem határozza meg egyértelműen, hogy in-memory megoldást vár el vagy fájlos tárolást, csak úgy fogalmaz, hogy a futó alkalmazás elmentse és vissza tudja olvasni a szavazást. Ezért az alkalmazás jelenleg in-memory megoldással működik, amely eleget tesz ennek a feltételnek. Azaz újraindítás után az adatok elvesznek.***
