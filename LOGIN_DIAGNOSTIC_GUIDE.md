# 🔍 Diagnostic - Problème "Accès Refusé"

## ⚠️ **Problème: Identifiants non reconnus**

L'application répond "accès refusé" lors de la tentative de connexion. Voici les causes possibles et solutions.

---

## 🛠️ **Nouvel APK avec Diagnostic Amélioré**

**Fichier:** `app/build/outputs/apk/debug/app-debug.apk`
**Nouveautés:** 
- ✅ Logs détaillés dans la console
- ✅ Interface de test API intégrée
- ✅ Messages d'erreur précis
- ✅ Diagnostic automatique

---

## 🔧 **Étapes de Diagnostic**

### 1. **Utiliser l'Interface de Test**
1. Lancez l'application
2. Cliquez sur **"🔧 Test de connexion API"** (sous "Configuration de l'API")
3. Testez la connexion API d'abord
4. Puis testez avec vos identifiants

### 2. **Vérifier les Logs**
Les logs détaillés apparaissent dans:
- **Console Android Studio** (si connecté via USB Debug)
- **Logcat** avec filtre "System.out"
- **Résultats dans l'interface de test**

---

## 🔍 **Causes Possibles du Problème**

### A. **Problème d'URL API**
**Symptôme:** "Serveur introuvable" ou "URL non trouvée"
**Solution:**
1. Vérifiez que `http://208.109.231.135:8000/` est accessible
2. Testez dans un navigateur: `http://208.109.231.135:8000/api/login/`
3. Changez l'URL via "Configuration de l'API"

### B. **Identifiants Incorrects**
**Symptôme:** Code 401 "Identifiants incorrects"
**Solution:**
1. Vérifiez username et password exacts
2. Respectez la casse (majuscules/minuscules)
3. Pas d'espaces avant/après les identifiants

### C. **Utilisateur Inexistant**
**Symptôme:** Code 403 "Accès refusé"
**Solution:**
1. L'utilisateur existe-t-il dans la base de données ?
2. L'utilisateur est-il actif ?
3. Le format des données est-il correct ?

### D. **Format de Réponse Incorrect**
**Symptôme:** Erreur de parsing JSON
**Solution:**
1. L'API renvoie-t-il le bon format ?
2. Le token est-il bien inclus ?
3. Le userType est-il bien "chauffeur" ou "demandeur" ?

---

## 🧪 **Test Manuel de l'API**

### Via cURL:
```bash
# Tester la connexion
curl -X POST http://208.109.231.135:8000/api/login/ \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=VOTRE_USERNAME&password=VOTRE_PASSWORD"
```

### Via Postman/Insomnia:
- **URL:** `http://208.109.231.135:8000/api/login/`
- **Method:** POST
- **Body:** x-www-form-urlencoded
- **Fields:** username, password

### Réponse Attendue:
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "first_name": "Test",
    "last_name": "User",
    "is_driver": true,
    "is_requester": false,
    "userType": "chauffeur"
  },
  "userType": "chauffeur"
}
```

---

## 📋 **Checklist de Vérification**

### ✅ **Côté Client (Android):**
- [ ] URL correcte: `http://208.109.231.135:8000/`
- [ ] Identifiants exacts (sans espaces)
- [ ] Internet disponible
- [ ] Permissions réseau accordées
- [ ] APK de debug installé

### ✅ **Côté Serveur (Django):**
- [ ] Serveur accessible sur le port 8000
- [ ] Endpoint `/api/login/` fonctionnel
- [ ] CORS configuré pour Android
- [ ] Utilisateur existe et est actif
- [ ] Format JSON correct

### ✅ **Base de Données:**
- [ ] Tables users et auth_token créées
- [ ] Utilisateur avec le bon username
- [ ] Mot de passe hashé correctement
- [ ] Champ `is_active = true`

---

## 🚨 **Messages d'Erreur et Solutions**

| Message | Cause | Solution |
|---------|-------|----------|
| **"Identifiants incorrects"** | Username/password faux | Vérifiez les identifiants |
| **"Accès refusé"** | Utilisateur inexistant/inactif | Créez/réactivez l'utilisateur |
| **"Serveur introuvable"** | URL incorrecte | Changez l'URL API |
| **"Délai d'attente"** | Serveur lent/inaccessible | Vérifiez le réseau |
| **"Réponse vide"** | Format réponse incorrect | Vérifiez l'API Django |

---

## 🔧 **Actions Immédiates**

### 1. **Installer le Nouvel APK**
```bash
# Désinstaller l'ancienne version
adb uninstall com.example.gestionvehicules

# Installer la nouvelle version avec diagnostic
adb install app-debug.apk
```

### 2. **Tester avec l'Interface de Test**
1. Ouvrir l'application
2. Cliquer sur "🔧 Test de connexion API"
3. Suivre les étapes de diagnostic

### 3. **Vérifier les Logs**
```bash
# Voir les logs en temps réel
adb logcat | grep "System.out"
```

---

## 📞 **Si le Problème Persiste**

### Informations à Collecter:
1. **Message d'erreur exact** affiché
2. **Logs de l'application** (via l'interface de test)
3. **Résultat du test cURL/Postman**
4. **Version Android** du téléphone
5. **Réseau utilisé** (WiFi/4G)

### Tests Additionnels:
- Essayer avec un autre utilisateur
- Tester depuis un autre appareil
- Vérifier avec un émulateur Android

---

## 🎯 **Solution la Plus Probable**

Le problème "accès refusé" est généralement dû à:
1. **Utilisateur inexistant** dans la base de données
2. **Mot de passe incorrect** ou mal hashé
3. **Endpoint API** inaccessible ou mal configuré

**Commencez par tester l'API directement** avec cURL/Postman pour isoler le problème ! 🚀
