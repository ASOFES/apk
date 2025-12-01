# Gestion Véhicules - Application Android

Application Android pour la gestion de véhicules avec deux modules principaux : Chauffeur et Demandeur.

## Architecture

- **MVVM** (Model-View-ViewModel)
- **Hilt** pour l'injection de dépendances
- **Retrofit** pour les appels API
- **Coroutines** pour les opérations asynchrones
- **ViewBinding** pour la liaison des vues

## Structure du Projet

```
app/
├── src/main/java/com/example/gestionvehicules/
│   ├── data/
│   │   ├── api/           # Configuration API et services
│   │   └── model/         # Modèles de données
│   ├── di/                 # Modules d'injection de dépendances
│   └── ui/
│       ├── auth/           # Écran de connexion
│       ├── driver/         # Interface chauffeur
│       ├── requester/      # Interface demandeur
│       ├── settings/       # Configuration de l'API
│       └── splash/         # Écran de démarrage
└── src/main/res/
    ├── layout/             # Fichiers de mise en page
    ├── values/             # Chaînes, couleurs, thèmes
    └── drawable/           # Images et icônes
```

## Configuration API

L'application supporte plusieurs URLs pour se connecter à votre API Django :

- **HTTPS** : `https://mamordc.cc/` (par défaut)
- **HTTP** : `http://mamordc.cc/`
- **IP avec port** : `http://208.109.231.135:8000/`
- **URL personnalisée** : Configurable par l'utilisateur

### Configuration de l'API

1. Depuis l'écran de connexion, cliquez sur "Configuration de l'API"
2. Sélectionnez l'URL souhaitée dans la liste
3. Optionnellement, entrez une URL personnalisée
4. Testez la connexion avec le bouton "Tester la connexion"
5. Sauvegardez la configuration
6. Redémarrez l'application pour appliquer les changements

## Fonctionnalités

### Module Chauffeur
- Connexion sécurisée
- Gestion des véhicules assignés
- Suivi des missions
- Maintenance et entretien

### Module Demandeur
- Connexion sécurisée
- Demande de cours
- Suivi des demandes
- Gestion du profil

### Configuration
- Support de multiples URLs d'API
- Test de connexion intégré
- Sauvegarde persistante des préférences
- Interface utilisateur intuitive

## Dépendances Principales

- AndroidX Core Libraries
- Material Design Components
- Retrofit & OkHttp
- Hilt (DI)
- Coroutines
- Glide (chargement d'images)

## Installation

1. Cloner le projet
2. Ouvrir dans Android Studio
3. Configurer l'URL de l'API si nécessaire via l'interface
4. Builder et exécuter

## Configuration Développeur

Pour modifier les URLs par défaut, éditez `ApiConfig.kt` :

```kotlin
object ApiConfig {
    const val BASE_URL_HTTP = "http://votre-url.com/"
    const val BASE_URL_HTTPS = "https://votre-url.com/"
    const val BASE_URL_IP = "http://ip:port/"
}
```

## Tests

L'application inclut des tests unitaires et d'interface utilisateur.

## Sécurité

- Support des connexions HTTPS et HTTP
- Gestion des tokens d'authentification
- Validation des URLs personnalisées
- Protection contre les attaques CSRF

## Licence

[Votre licence]
