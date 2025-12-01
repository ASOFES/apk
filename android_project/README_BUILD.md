# Driver App - Guide de compilation

## Prérequis

- Android Studio (dernière version)
- JDK 8 ou supérieur
- Android SDK (API 21 minimum)

## Étapes de compilation

### 1. Importer le projet

1. Ouvrir Android Studio
2. File → Open → Sélectionner le dossier `android_project`
3. Attendre la synchronisation Gradle

### 2. Configuration

1. Ouvrir `app/build.gradle`
2. Vérifier que le `applicationId` correspond à vos besoins
3. Modifier l'URL de l'API si nécessaire dans `DriverApplication.java`

### 3. Générer l'APK

#### Pour le développement

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Choisir "debug"
3. L'APK sera généré dans `app/build/outputs/apk/debug/`

#### Pour la production

1. Build → Generate Signed Bundle / APK
2. Choisir "APK"
3. Créer ou utiliser une clé de signature
4. L'APK signé sera généré dans `app/release/`

### 4. Tester l'APK

1. Transférer l'APK sur un appareil Android
2. Activer "Installation d'applications inconnues"
3. Installer et tester avec les identifiants d'un chauffeur

## Configuration de l'API

L'URL de l'API est configurée dans `DriverApplication.java` :

```java
private static final String BASE_URL = "http://135-231-109-208.host.secureserver.net:8000/";
```

Modifiez cette URL pour pointer vers votre serveur Django.

## Identifiants de test

Utilisez les identifiants d'un chauffeur existant dans votre base de données Django.

## Dépannage

- Erreur de connexion : Vérifiez l'URL et la connectivité réseau
- Token invalide : L'APK gère automatiquement la reconnexion
- Erreur SSL : Pour le développement en HTTP, `android:usesCleartextTraffic="true"` est déjà configuré
