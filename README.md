# Pothole Detection

## Descrizione del progetto

Il progetto Pothole detection implementa un sistema classificazione di buche, con l'uso della SensorTile.box PRO di STMicroelectronics. Il sistema è in grado di rilevare in tempo reale le presenza di buche durante la guida. Oltre all'identificazione il sistema permette di tracciare la posizione e inviare una segnalazione ad un sistema ml che identifica lo stato di salute della strada.

## Tecnologie utilizzate

   -  SensorTile.box PRO: La scheda di STMicroelectronics è progettata per prototipi di monitoraggio ambientale e rilevamento dei movimenti. Integra sensori come accelerometro, giroscopio, magnetometro, sensori di temperatura, pressione e un microfono digitale. Dispone di un microcontrollore per l'elaborazione e supporta Bluetooth Low Energy (BLE) e NFC. È compatibile con l'SDK BlueST-SDK disponibile alla repository: BlueSTSDK_Android

## Funzionalità 


L'applicazione Android permette di:
   - Connettersi alla SensorTile.box PRO tramite Bluetooth Low Energy.
   - Visualizzare i dati grezzi dell'accelerometro, del magnetometro e del giroscopio in tempo reale.
   - Visualizzare le previsioni riconosciute dalla SensorTile.box PRO in tempo reale.
   - Effettuare segnalazione contentenente i dati dell'accelerometro,giroscopio e coordinate GPS ad un servizio che tramite un modello di Random Forest pre-addestrato traccia lo stato di salute delle strade.


## Funzionalità tramite Backend

Tramite Backend, l'applicazione è dotata di diverse funzionalità aggiuntive:

    - Registrazione e autenticazione degli utenti.
    - Memorizzazione dei riconoscimenti delle buchein un database remoto.
    - Visualizzazione di uno storico delle buche globale e indicizzato per città.
