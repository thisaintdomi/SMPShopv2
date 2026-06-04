# VirtualShop

Minecraft Spigot plugin — virtuális páncélállvány bolt FancyHologram támogatással.

## Funkciók

- Virtuális boltok létrehozása páncélállványokkal
- FancyHolograms integráció (automatikusan detectálja; ha nincs, saját ArmorStand hologramot használ)
- Vásárlás jobb klikkel
- Admin szerkesztő GUI (shift + jobb klikk)
- Ár módosítás, áthelyezés, törlés
- Vault integráció (economy)
- `/spawn` és `/shop` teleport parancsok
- Adatok YAML-ban mentve (szerver újraindítás után is megmaradnak)

## Telepítés

1. Másold a `VirtualShop-*.jar` fájlt a `plugins/` mappába
2. **Vault** + gazdasági plugin szükséges (pl. EssentialsX)
3. Opcionálisan: **FancyHolograms** jobb hologramokért
4. Szerver újraindítás

## Parancsok

| Parancs | Leírás |
|---|---|
| `/shopitemsave <id>` | Kézben lévő tárgy mentése |
| `/shopgive <id>` | Mentett tárgy átvétele |
| `/shopcreate <id> <ár>` | Bolt létrehozása |
| `/shopremove <id>` | Bolt törlése |
| `/shoplist` | Boltok listázása |
| `/shopmove` | Bolt áthelyezése jelenlegi pozícióra |
| `/shopprice <ár>` | Bolt árának módosítása |
| `/spawn` | Teleport spawnra |
| `/shop` | Teleport shopra |

## Jogosultságok

| Jog | Leírás |
|---|---|
| `virtualshop.admin` | Admin parancsok (alapból: op) |

## Admin GUI

Shift + jobb klikk egy bolt páncélállványra az admin GUI megnyitásához:
- **Ár módosítása** — új ár beállítása `/shopprice` paranccsal
- **Áthelyezés** — bolt mozgatása jelenlegi pozícióra `/shopmove` paranccsal
- **Törlés** — bolt végleges törlése

## config.yml

```yaml
shop-world: "SMPspawn"

spawn-location:
  world: "SMPspawn"
  x: 8
  y: 17
  z: 5

shop-location:
  world: "SMPspawn"
  x: -10
  y: 24
  z: 313

teleport-countdown: 3
buy-cooldown-ticks: 20

hologram:
  lines:
    - "&8[ &a{id} &8]"
    - "&7Ár: &a{price}$"
    - "&7Jobb klikk a vásárláshoz"
  y-offset: 1.8
```

## Build

```bash
mvn clean package
```

A kész JAR a `target/` mappában lesz.

## GitHub Actions

Minden push-ra automatikusan buildelődik. Tag pushnál (pl. `v1.0.0`) automatikus GitHub Release készül a JAR-ral.
