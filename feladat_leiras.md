# Feladat-leírás

A Hivatal a parlamenti törvényhozói munka támogatását végzi. Többek között a szavazások adatainak rögzítését és lekérdezhetőségét biztosítja. Ezzel kapcsolatos a megoldandó fiktív feladat.

Egy olyan Spring Boot alapú alkalmazás REST felületét kell elkészíteni, amely alkalmas

- egy szavazás adatainak rögzítésére

- egy képviselő adott szavazáson leadott szavazatának lekérdezésére

- a szavazás eredményének (elfogadott/elutasított) kiszámolására

- szavazási kimutatások készítésére

## A részfeladatok

1. egy szavazás adatainak elmentése

2. egy képviselő adott szavazáson leadott szavazatának lekérdezése

3. a szavazás eredményének kiszámolása

4. adott napra a szavazások és eredményeik lekérdezése

5. szavazási kimutatások készítése

## A feladatok részletes leírása

### 1 Egy szavazás adatainak elmentése

Adottak a szavazás adatai egy json-ként, amelynek adattagjai:

- „idopont”: másodpercre pontos időpont ISO 8601 formában megadva

- „targy”: szavazás tárgya szöveges adatként

- „tipus”: a szavazás típusának kódja. Ez lehet „j” (jelenlét), „e” (egyszerű többségi szavazás), „m” (minősített többségi szavazás)

- „eljaras”: a szavazás milyen eljárás keretében történt („n”: normál, „s”: sürgősségi, „k”: kivételes, „e”: szabályzattól eltérő)

- „elnok”: a szavazást vezető elnök, aki a képviselők közül kerül ki

- „szavazatok”: a leadott képviselői szavazatok json-tömb formában

> **MEGJEGYZÉS** A szavazáson az ülést vezető elnök is szavaz.

A leadott szavazatok adattagjai:

- „kepviselo”: az szavazó képviselő kódja

- „szavazat”: a leadott szavazat, amely lehet „i” (igen), „n” (nem), „t” (tartózkodás)

A fenti definícióhoz tartozó JSON séma:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "idopont": {
      "type": "string",
      "format": "date-time"
    },
    "targy": {
      "type": "string"
    },
    "tipus": {
      "type": "string",
      "enum": [
        "j",
        "e",
        "m"
      ]
    },
    "eljaras": {
      "type": "string",
      "enum": [
        "n",
        "s",
        "k",
        "e"
      ]
    },
    "elnok": {
      "type": "string"
    },
    "szavazatok": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "kepviselo": {
            "type": "string"
          },
          "szavazat": {
            "type": "string",
            "enum": [
              "i",
              "n",
              "t"
            ]
          }
        },
        "required": [
          "kepviselo",
          "szavazat"
        ]
      }
    }
  },
  "required": [
    "idopont",
    "targy",
    "tipus",
    "elnok",
    "szavazatok"
  ]
}
```

Példa adat:

```json
{
  "idopont": "2023-09-28T11:06:25Z",
  "targy": "Szavazás tárgya",
  "tipus": "j",
  "eljaras": "n",
  "elnok": "Kepviselo1",
  "szavazatok": [
    {
      "kepviselo": "Kepviselo1",
      "szavazat": "i"
    },
    {
      "kepviselo": "Kepviselo2",
      "szavazat": "n"
    },
    {
      "kepviselo": "Kepviselo3",
      "szavazat": "t"
    }
  ]
}
```

A REST request útvonala: /szavazasok/szavazas

Ellenőrizze a program, hogy

- megfelelő struktúrájú-e a feltöltött adat

- az elnöknek van-e szavazata

- egy képviselő csak egyszer szavazhat

- ugyanarra az időpontra nem lehet két szavazást felvinni

Ha ellenőrzéskor hiba fordul elő, akkor ne történjen meg a rögzítés. A program adjon vissza hibát a hiba okának leírásával JSON formátumban. Megfelelő küldött adat esetén rögzítéskor adjon egyedi, „url-barát” (az url-be változtatás, encoding használata nélkül beírható) azonosítót a szavazásnak és azt a REST response-ban adja vissza.

Ennek JSON sémája

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "szavazasId": {
      "type": "string"
    }
  },
  "required": [
    "szavazasId"
  ]
}
```

Példa:

```json
{
  "szavazasId": "OJ7251"
}
```

### 2 Egy képviselő adott szavazáson leadott szavazatának lekérdezése

Az 1. feladat kiegészítése.

A REST request útvonala: /szavazasok/szavazat

A paraméterek az url-ben legyenek megadhatóak. A lekérés paraméterei:

- a szavazás azonosítója (paraméter neve: szavazas)

- a képviselő azonosítója (paraméter neve: kepviselo)

A választ JSON formájában adja vissza a program, amelynek sémája:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "szavazat": {
      "type": "string"
    }
  },
  "required": [
    "szavazat"
  ]
}
```

Példa:

```json
{
  "szavazat": "i"
}
```

Amennyiben nincs ilyen szavazás vagy az adott szavazáson nem szavazott a képviselő, akkor 404-es HTTP státusszal jelezze ezt a program.

### 3 A szavazás eredményének (elfogadott/elutasított) kiszámolása

A 2. feladat kiegészítése.

A szavazás eredményét az alábbi módon definiáljuk:

- jelenlét: csak a jelenlevő képviselők létszámának megállapítására szolgál, eredménye mindig elfogadott

- egyszerű: eredménye elfogadott, ha a jelenlevő képviselők több mint fele igennel szavazott, egyébként elutasított.

- minősített: eredménye elfogadott, ha az összes képviselő több mint fele igennel szavazott. Az összes képviselő létszáma adott. Jelenleg 200 fő.

> **MEGJEGYZÉS** A jelenlevő képviselők száma változhat. Mindig a szavazást megelőző legutolsó jelenléti szavazáson érvényesen (igen, nem, tartózkodás) szavazó képviselők száma adja a jelenlevők számát.

Legyen lekérhető egy szavazás eredménye, paraméterként az url-ben legyen megadható a szavazás egyedi azonosítója! Amennyiben nincs a megadott azonosítóval szavazás, akkor a visszaadott válasz legyen 404-es HTTP kód!

Meglevő adat esetén a visszaadott információ:

- a szavazás eredménye (elfogadott esetén : F, elutasított esetén U)

- a képviselők száma (a szavazás eredményének kiszámításakor figyelembe vett képviselők száma)

- az igenek száma

- a nemek száma

- a tartózkodások száma

A kérés útvonala: /szavazasok/eredmeny

A válasz JSON sémája:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "eredmeny": {
      "type": "string",
      "enum": [
        "F",
        "U"
      ]
    },
    "kepviselokSzama": {
      "type": "integer",
      "minimum": 0
    },
    "igenekSzama": {
      "type": "integer",
      "minimum": 0
    },
    "nemekSzama": {
      "type": "integer",
      "minimum": 0
    },
    "tartozkodasokSzama": {
      "type": "integer",
      "minimum": 0
    }
  },
  "required": [
    "eredmeny",
    "kepviselokSzama",
    "igenekSzama",
    "nemekSzama",
    "tartozkodasokSzama"
  ]
}
```

Példa:

```json
{
  "eredmeny": "F",
  "kepviselokSzama": 150,
  "igenekSzama": 120,
  "nemekSzama": 30,
  "tartozkodasokSzama": 0
}
```

### 4 Adott napra a szavazások és eredményeik lekérdezése

Legyen lehetőség egy adott napon megtartott és regisztrált szavazások eredményeinek lekérdezésésre! A paraméter legyen megadható az url-ben! Amennyiben nincs az adott napon szavazás, akkor a válasz egy üres tömb legyen.

A kérés útvonala: /szavazasok/napi-szavazasok

A válasz JSON sémája legyen:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "szavazasok": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "idopont": {
            "type": "string",
            "format": "date-time"
          },
          "targy": {
            "type": "string"
          },
          "tipus": {
            "type": "string",
            "enum": [
              "j",
              "e",
              "m"
            ]
          },
          "eljaras": {
            "type": "string",
            "enum": [
              "n",
              "s",
              "k",
              "e"
            ]
          },
          "elnok": {
            "type": "string"
          },
          "eredmeny": {
            "type": "string",
            "enum": [
              "F",
              "U"
            ]
          },
          "kepviselokSzama": {
            "type": "integer",
            "minimum": 0
          },
          "szavazatok": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "kepviselo": {
                  "type": "string"
                },
                "szavazat": {
                  "type": "string"
                }
              },
              "required": [
                "kepviselo",
                "szavazat"
              ]
            }
          }
        },
        "required": [
          "idopont",
          "targy",
          "tipus",
          "elnok",
          "szavazatok"
        ]
      }
    }
  },
  "required": [
    "szavazasok"
  ]
}
```

Példa:

```json
{
  "szavazasok": [
    {
      "idopont": "2023-12-13T14:30:00Z",
      "targy": "Szavazás tárgya1",
      "tipus": "m",
      "eljaras": "n",
      "elnok": "kepviselo1",
      "eredmeny": "F",
      "kepviselokSzama": 150,
      "szavazatok": [
        {
          "kepviselo": "kepviselo1",
          "szavazat": "i"
        },
        {
          "kepviselo": "kepviselo2",
          "szavazat": "n"
        },
        {
          "kepviselo": "kepviselo3",
          "szavazat": "i"
        },
        {
          "kepviselo": "kepviselo4",
          "szavazat": "t"
        }
      ]
    },
    {
      "idopont": "2023-12-14T10:00:00Z",
      "targy": "Szavazás tárgya2",
      "tipus": "e",
      "eljaras": "s",
      "elnok": "kepviselo3",
      "eredmeny": "U",
      "kepviselokSzama": 130,
      "szavazatok": [
        {
          "kepviselo": "kepviselo1",
          "szavazat": "i"
        },
        {
          "kepviselo": "kepviselo3",
          "szavazat": "n"
        },
        {
          "kepviselo": "kepviselo2",
          "szavazat": "i"
        }
      ]
    }
  ]
}
```

### 5 Kimutatások készítése

#### 5.1 Részvétel

Legyen lehetőség egy adott időszakra lekérdezni, hogy egy képviselő átlagosan hány szavazáson vett részt (a jelenléti szavazásokat nem figyelembe véve)! Az eredményt elegendő 2 tizedes pontossággal megadni. A paramétereket (időszak kezdete, időszak vége) az url-ben kell megadni.

A kérés útvonala: /szavazasok/kepviselo-reszvetel-atlag

A válasz JSON sémája:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "atlag": {
      "type": "number"
    }
  },
  "required": [
    "atlag"
  ]
}
```

Példa:

```json
{
  "atlag": 4.25
}
```

#### 5.2 Különleges eljárásokban megtartott szavazások száma adott időszakban

Legyen lehetőség egy adott időszakra lekérdezni, hogy hány különleges eljárásban megtartott szavazás történt és azok közül mennyi volt elfogadott és mennyi elutasított szavazás. A különleges eljárások a következők: sürgősségi, kivételes, szabályzattól eltérő. A paramétereket (időszak kezdete, időszak vége) az url-ben kell megadni-.

A kérés útvonala: /szavazasok/kulonleges-eljarasok-szama

A válasz JSON sémája:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "szavazasok": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "eljaras": {
            "type": "string",
            "enum": [
              "s",
              "k",
              "e"
            ]
          },
          "eredmeny": {
            "type": "string",
            "enum": [
              "F",
              "U"
            ]
          },
          "szam": {
            "type": "integer",
            "minimum": 0
          }
        },
        "required": [
          "eljaras",
          "eredmeny",
          "szam"
        ]
      }
    }
  },
  "required": [
    "szavazasok"
  ]
}
```

Példa:

```json
{
  "szavazasok": [
    {
      "eljaras": "s",
      "eredmeny": "F",
      "szam": 3
    },
    {
      "eljaras": "s",
      "eredmeny": "U",
      "szam": 1
    },
    {
      "eljaras": "k",
      "eredmeny": "F",
      "szam": 2
    },
    {
      "eljaras": "k",
      "eredmeny": "U",
      "szam": 0
    },
    {
      "eljaras": "e",
      "eredmeny": "F",
      "szam": 1
    },
    {
      "eljaras": "e",
      "eredmeny": "U",
      "szam": 1
    },
    {
      "eljaras": "összes",
      "eredmeny": "F",
      "szam": 6
    },
    {
      "eljaras": "összes",
      "eredmeny": "U",
      "szam": 2
    },
    {
      "eljaras": "összes",
      "eredmeny": "összes",
      "szam": 8
    }
  ]
}
```

### 6 Docker konténer építése az alkalmazásnak

Az építőeszközben legyen egy task, amely a programból docker image-et épít

A részfeladatok egymásra épülnek. Sorban kell megoldani őket. Az együttműködésnek nem feltétele az összes részfeladat megoldása.

### 7 A kivitelezéshez használandó eszközök

- Java verzió: 21

- Építő eszköz: gradle

- Adatbázis: H2 db

- REST felület: Spring Boot

- Adatelérés: JPA/Hibernate

Az elkészült programot töltse fel a GitHub-ra és annak elérését küldje vissza nekünk!

Cél egy IDE független kódbázis előállítása. Csak a fejlesztéshez szükséges file-okat töltse fel!

Kérjük, saját tudását használja! Mesterséges intelligencia eszközöket csak átgondoltan vegyen igénybe, hiszen a feladat sikeres megoldása után, amennyiben személyes elbeszélgetésre kerül sor, szeretnénk a megoldás menetéről, a kapott program egyes részleteiről beszélgetni.
