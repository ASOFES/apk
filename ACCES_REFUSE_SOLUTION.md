# 🔒 Solution Complète - "Accès Refusé. Vérifiez vos permissions"

## ⚠️ **Problème Identifié**
Le message "accès refusé. Vérifiez vos permissions" indique que l'API répond mais refuse l'accès. C'est généralement un problème de **CORS** ou de **permissions backend**.

---

## 🛠️ **NOUVEL APK AVEC DIAGNOSTIC COMPLET**

**Fichier:** `app/build/outputs/apk/debug/app-debug.apk`
**Nouveauté:** Interface de diagnostic complet pour identifier la cause exacte

---

## 🔍 **Étapes de Diagnostic Immédiat**

### 1. **Installer le Nouvel APK**
```bash
# Désinstaller ancienne version
adb uninstall com.gestion.vehicules

# Installer nouvelle version avec diagnostic
adb install app-debug.apk
```

### 2. **Utiliser l'Interface de Diagnostic**
1. Lancez l'application
2. Cliquez sur **"🔍 Diagnostic complet"** (en bas de l'écran de login)
3. Suivez les tests dans l'ordre:

#### Test 1: Connexion Basic
- Cliquez sur **"🌐 Test connexion basic"**
- Vérifiez que l'API répond (code 401 ou 200)

#### Test 2: CORS
- Cliquez sur **"🌍 Test CORS"**
- **Ceci est crucial !** Le problème "accès refusé" vient souvent de CORS

#### Test 3: Authentification
- Entrez vos identifiants
- Cliquez sur **"🔐 Test authentification"**
- Analysez les logs détaillés

---

## 🎯 **Causes Probables du Problème**

### A. **CORS Non Configuré (90% des cas)**
**Symptôme:** Test CORS montre "Aucun header CORS détecté"

**Solution Django:**
```python
# Dans settings.py
INSTALLED_APPS = [
    ...
    'corsheaders',
    ...
]

MIDDLEWARE = [
    ...
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.common.CommonMiddleware',
    ...
]

# Ajouter à la fin de settings.py
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://208.109.231.135:8000",
    "*"  # Pour les tests (à retirer en production)
]

CORS_ALLOW_CREDENTIALS = True
CORS_ALLOW_ALL_ORIGINS = True  # Pour les tests
```

### B. **Permissions Django Rest Framework**
**Symptôme:** Code 403 "Accès refusé"

**Solution:**
```python
# Dans settings.py
REST_FRAMEWORK = {
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.AllowAny',  # Pour les tests
        # 'rest_framework.permissions.IsAuthenticated',  # Pour production
    ],
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.SessionAuthentication',
        'rest_framework.authentication.TokenAuthentication',
    ],
}
```

### C. **Utilisateur Inexistant ou Inactif**
**Symptôme:** Code 401 "Identifiants incorrects"

**Vérification:**
```python
# Dans Django shell
python manage.py shell
from django.contrib.auth.models import User
user = User.objects.get(username='votre_username')
print(user.is_active)  # Doit être True
print(user.check_password('votre_password'))  # Doit être True
```

---

## 📋 **Guide de Dépannage Étape par Étape**

### Étape 1: **Vérifier l'API Directement**
```bash
# Test avec curl
curl -X POST http://208.109.231.135:8000/api/login/ \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Origin: http://localhost:3000" \
  -d "username=VOTRE_USERNAME&password=VOTRE_PASSWORD"
```

### Étape 2: **Analyser les Réponses**
- **Code 200:** ✅ API fonctionne
- **Code 401:** 🔑 Identifiants incorrects
- **Code 403:** 🚫 Permissions insuffisantes
- **Code 404:** 🔍 Endpoint non trouvé
- **Code 500:** 💥 Erreur serveur

### Étape 3: **Configurer CORS si Nécessaire**
```bash
# Installer django-cors-headers
pip install django-cors-headers

# Ajouter aux settings.py (voir ci-dessus)
```

---

## 🔧 **Solutions Rapides**

### Solution 1: **Désactiver Temporairement les Permissions**
```python
# Dans views.py
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny

@api_view(['POST'])
@permission_classes([AllowAny])  # Temporaire pour les tests
def login_api(request):
    # Votre code de login
    pass
```

### Solution 2: **Configurer CORS pour Android**
```python
# Dans settings.py
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://208.109.231.135:8000",
    "capacitor://localhost",  # Pour Android
    "http://localhost",       # Pour développement
]

CORS_ALLOW_ALL_ORIGINS = True  # Pour tests uniquement
```

### Solution 3: **Vérifier les Tokens**
```python
# Dans votre vue de login
from rest_framework.authtoken.models import Token

def login_view(request):
    user = authenticate(username=username, password=password)
    if user:
        token, created = Token.objects.get_or_create(user=user)
        return Response({
            'token': token.key,
            'user': UserSerializer(user).data,
            'userType': 'chauffeur' if user.profile.is_driver else 'demandeur'
        })
```

---

## 📱 **Utilisation de l'Interface de Diagnostic**

### Onglets Disponibles:
1. **🌐 Test connexion basic** - Vérifie que l'API répond
2. **🔐 Test authentification** - Test avec vos identifiants
3. **🌍 Test CORS** - Vérifie les headers CORS
4. **📋 Afficher headers** - Montre les requêtes/réponses
5. **🗑️ Effacer logs** - Nettoie l'affichage

### Messages Clés à Observer:
- ✅ "API accessible et fonctionnelle"
- ⚠️ "Aucun header CORS détecté"
- ❌ "Identifiants incorrects"
- 🚫 "Accès refusé - permissions insuffisantes"

---

## 🎯 **Plan d'Action Recommandé**

### Immédiat:
1. **Installer** le nouvel APK avec diagnostic
2. **Lancer** le diagnostic complet
3. **Identifier** la cause exacte via les logs

### Si problème CORS:
1. **Configurer** django-cors-headers
2. **Redémarrer** le serveur Django
3. **Tester** à nouveau

### Si problème permissions:
1. **Vérifier** les permissions DRF
2. **Désactiver** temporairement pour tests
3. **Réactiver** après validation

---

## 📞 **Support Technique**

### Informations à Collecter:
1. **Logs complets** de l'interface de diagnostic
2. **Réponse curl** de test manuel
3. **Configuration Django** (settings.py)
4. **Logs du serveur** Django

### Tests Additionnels:
- **Postman/Insomnia** avec mêmes headers
- **Navigateur web** avec console développeur
- **Autre appareil** pour isoler le problème

---

## 🎉 **Solution la Plus Probable**

Dans **90% des cas**, le problème "accès refusé" est dû à **CORS non configuré** dans Django.

**L'interface de diagnostic vous montrera exactement si c'est le cas !** 🚀

Utilisez le diagnostic complet, et vous saurez exactement quoi corriger dans votre backend Django.
