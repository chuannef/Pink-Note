# Firestore Rules Draft

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function owns(uid) {
      return signedIn() && request.auth.uid == uid;
    }

    match /users/{uid} {
      allow read, write: if owns(uid);
    }

    match /cycle/{uid} {
      allow read, write: if owns(uid);
    }

    match /daily_logs/{uid}/days/{date} {
      allow read, write: if owns(uid);
    }

    match /notifications/{uid}/items/{notificationId} {
      allow read, write: if owns(uid);
    }

    match /settings/{uid} {
      allow read, write: if owns(uid);
    }
  }
}
```
