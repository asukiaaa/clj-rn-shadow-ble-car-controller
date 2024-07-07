# clj-rn-shadow-ble-car-controller

An application for smartphone to control a mini car with via ble.

## setup

```sh
npm i
```

## debug

```sh
npx shadow-cljs watch app
```

On other terminal.
```sh
npm run android
```

## build on local

```sh
npx shadow-cljs release app # not needed?
eas build --platform android --local
```

## References
- [PEZ/rn-rf-shadow](https://github.com/PEZ/rn-rf-shadow)
