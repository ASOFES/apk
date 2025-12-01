# 🔍 Guide de Débogage - Pas de Connexion

## ⚠️ **Problème: Toujours Pas de Connexion**

D'après vos captures d'écran, l'application ne parvient pas à se connecter. Analysons étape par étape.

---

## 🔍 **Étapes de Diagnostic Immédiat**

### Étape 1: **Utiliser le Diagnostic Complet**
1. **Lancez l'application**
2. **Cliquez sur "🔍 Diagnostic complet"** (en bas de l'écran de login)
3. **Suivez les tests dans l'ordre:**

#### Test 1: Connexion Basic
- Cliquez sur **"🌐 Test connexion basic"**
- **Résultat attendu:** Code 200 ou 401 (signifie que le serveur répond)
- **Si erreur:** Problème de réseau ou URL incorrecte

#### Test 2: Test d'Authentification
- Entrez vos identifiants exacts
- Cliquez sur **"🔐 Test authentification"**
- **Analysez les logs détaillés**

#### Test 3: Test CORS
- Cliquez sur **"🌍 Test CORS"**
- **Vérifiez les headers CORS**

---

## 🎯 **Causes Possibles du Problème**

### A. **Problème Réseau**
**Symptôme:** "Serveur introuvable" ou "Délai d'attente"
**Solutions:**
- Vérifiez WiFi/4G
- Essayez un autre réseau
- Testez l'URL dans un navigateur

### B. **Problème d'Identifiants**
**Symptôme:** Code 401 "Identifiants incorrects"
**Solutions:**
- Vérifiez username exact (sensible à la casse)
- Vérifiez password exact
- Pas d'espaces avant/après

### C. **Problème CORS**
**Symptôme:** "Accès refusé" ou "CORS error"
**Solutions:**
- Configurez CORS dans Django
- Ajoutez les headers nécessaires

### D. **Problème Backend**
**Symptôme:** Code 500 ou erreur serveur
**Solutions:**
- Vérifiez les logs Django
- Redémarrez le serveur

---

## 📋 **Tests Manuels à Faire**

### Test 1: **URL dans Navigateur**
Ouvrez dans un navigateur: `http://mamordc.cc:8000/`
- **Doit afficher:** Page Django (même erreur 404 est OK)
- **Si ne charge pas:** Problème réseau/DNS

### Test 2: **Test cURL**
```bash
curl -X POST http://mamordc.cc:8000/login/ \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=VOTRE_USERNAME&password=VOTRE_PASSWORD"
```

### Test 3: **Vérification Backend**
```python
# Dans Django shell
python manage.py shell
from django.contrib.auth.models import User
user = User.objects.get(username='votre_username')
print(f"User exists: {user.exists()}")
print(f"Is active: {user.is_active}")
print(f"Password check: {user.check_password('votre_password')}")
```

---

## 🔧 **Solutions Rapides**

### Solution 1: **Vérifier les Logs Android**
```bash
# Si connecté via USB
adb logcat | grep "System.out"
```

### Solution 2: **Changer d'URL**
1. **Cliquez sur "Configuration de l'API"**
2. **Essayez une autre URL:**
   - `https://mamordc.cc/`
   - `http://mamordc.cc/`
   - URL personnalisée

### Solution 3: **Configuration CORS Django**
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
    ...
]

CORS_ALLOW_ALL_ORIGINS = True  # Pour tests
CORS_ALLOW_CREDENTIALS = True
```

---

## 📱 **Utilisation de l'Interface de Diagnostic**

### Messages à Observer:
- ✅ **"API accessible et fonctionnelle"** → Serveur OK
- ⚠️ **"Aucun header CORS détecté"** → Configurez CORS
- ❌ **"Identifiants incorrects"** → Vérifiez username/password
- 🚫 **"Accès refusé"** → Permissions Django

### Logs à Copier:
1. **Faites les tests dans l'interface**
2. **Copiez tous les logs** affichés
3. **Envoyez-les pour analyse**

---

## 🎯 **Plan d'Action**

### Immédiat:
1. **Lancez "🔍 Diagnostic complet"**
2. **Testez connexion basic**
3. **Testez authentification**
4. **Copiez les logs**

### Si Problème Persiste:
1. **Testez l'URL dans navigateur**
2. **Vérifiez les identifiants dans Django**
3. **Configurez CORS si nécessaire**
4. **Essayez une autre URL**

---

## 📞 **Informations à Collecter**

### Pour le Support:
1. **Logs complets** du diagnostic
2. **Résultat du test navigateur**
3. **Identifiants testés** (username)
4. **Réseau utilisé** (WiFi/4G)
5. **Version Android**

### Tests à Fournir:
- **Logs de l'interface de diagnostic**
- **Résultat cURL** si possible
- **Capture d'écran** des erreurs

---

## 🚀 **Solutions Probables**

### 90% des cas:
- **CORS non configuré** dans Django
- **Identifiants incorrects** ou utilisateur inexistant
- **URL incorrecte** ou serveur inaccessible

### 10% des cas:
- **Problème réseau** local
- **Firewall** bloquant les requêtes
- **Configuration Django** incorrecte

---

## 💡 **Action Immédiate**

**Utilisez l'interface "🔍 Diagnostic complet" et copiez les logs détaillés !**

Cela montrera exactement où est le problème et comment le résoudre. 🎯
