# 🏞️ TrailExplorer – Wildlife & Hiking Recommendation System  
_A JavaFX-based interactive trail & wildlife exploration app_

TrailExplorer is a desktop application that helps users **discover hiking trails, explore local wildlife, and join group activities**, powered by a clean MVC architecture, custom ADTs, and JavaFX UI.

The entire UI, multiple modules, and recommendation logic were designed & implemented by me.

---

## ✨ Features

### 🏠 HOME (Landing)
- Quick search for trails
- “Nearby / Popular / Personal Rec” panels
- Wildlife & group entry shortcuts

### 🔍 Trails Module
- Trail search (keyword + filters)
- Trail detail page (route, map, info)
- Monthly recommendations
- Dynamic trail cards

### 🐾 Wildlife Module
- 8 Animal groups (Mammal / Bird / Reptile / Fish / Insect…)
- Animal detail page  
  - Image carousel  
  - Recent sightings  
  - Movement calendar  
  - Map pins  
- Animal-based trail recommendation (linked to ADT system)

### 👥 Groups Module
- Create / join hiking groups
- Group detail page  
- Recommended groups algorithm
- Group create & search features

### 🙋‍♀️ Mine Module
- Saved trails
- Joined groups
- Viewing history (powered by custom Stack ADT)

---

# 🖼️ UI Flowchart (Full Application Structure)

<img width="9600" height="8800" alt="flowchartt 1" src="https://github.com/user-attachments/assets/0154d346-588a-4589-9fde-2976217bf4e0" />

---

# ⚙️ System Architecture
<img width="3600" height="4800" alt="adddt 1" src="https://github.com/user-attachments/assets/388ff0a3-fe60-436e-a6ad-5976500cab8e" />



### 🧠 Core Components
- **TrailIndex + TrailList**  
  Custom ADTs for trail indexing & list operations  
- **AnimalRecommendationService**  
  PriorityQueue + Relaxation strategy for trail ranking  
- **AnimalHistoryStack**  
  Custom LIFO stack tracking user viewing history  
- **TrailAnimalMatcher**  
  Multi-step filtering: terrain match + context match + metadata match  
- **GlobalData Loader**  
  Loads & indexes animals, trails, and groups

---

# 🚀 Tech Stack

### **Frontend (Desktop UI)**
- JavaFX 25
- FXML Layouts
- SceneBuilder UI design

### **Backend / Logic**
- Java 17+
- Custom ADTs (Stack, PriorityQueue wrapper, TrailList)
- Comparator-based ranking
- HashMap-based terrain matching

### **Data**
- JSON (animals, trails)
- CSV (groups)

---

# 📷 Screenshots

### 🐾 Animal Detail Page  

<img width="260" height="455" alt="36ea69e9ddbb7e1f0184653a28a63b30" src="https://github.com/user-attachments/assets/5a85eea0-97c3-4879-b79b-2f88f6900003" />

### 🥾 Trail Results  
<img width="260" height="455" alt="da70f36b3c60ddf797e5ef4348df8ff0" src="https://github.com/user-attachments/assets/84adc5b2-24fd-4d07-b4a8-96c9fdaea785" />


---

# 📌 My Contributions

### 🟢 **UI / UX**
- Designed complete user flow & UI prototypes  
- Built all FXML pages used in Wildlife module  
- Integrated navigation system (AppNavigator)

### 🟢 **Core Modules**
- Implemented **AnimalRecommendationService**  
- Built **AnimalHistoryStack (custom ADT)**  
- Designed & implemented **Wildlife → Trail → Group** cross-module linkage  
- Constructed **trail match + multi-step filtering** pipeline  
- Implemented **Recent Sighting** → “Trail detail” linkage  
- Developed **Recommend Trail button logic**

### 🟢 **Other**
- Built full flowchart diagrams  
- Helped teammates debug controller navigation  
- Managed global data loading integration

---

# 🧭 Future Work

- Add user accounts synced to cloud  
- Add real-time trail weather API  
- Add wildlife movement prediction based on season  
- Enable group chat & live location share  
- Improve recommendation model w/ extra features

---

# 📬 Contact  
**Jovie Wang**  
📧 wonderjiayi@gmail.com  
🔗 https://www.linkedin.com/in/jiayi-wang-425726330  

---

# 🌟 Thanks for reading!

If you’d like to explore the code, feel free to clone or fork the project ❤️  

