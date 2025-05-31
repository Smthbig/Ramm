<h1>🏥 Ramm Healthcare – Smart Clinic Management App</h1>

<p><strong>Ramm Healthcare</strong> is a feature-rich Android app built to simplify patient and medicine record management for small clinics. It enables quick patient registration, medicine billing, visit tracking, and editable visit history — all in an intuitive offline-first experience following <strong>Material You (Material 3)</strong> design principles.</p>

<hr>

<h2>🚀 Features</h2>

<h3>🧑‍⚕️ Patient Visit Management</h3>
<ul>
  <li>Add or update patient details: name, age (in Y/M/D), and visit date.</li>
  <li>Track visit fee payment mode (Cash/UPI/Online).</li>
  <li>Auto-formatted visit history view.</li>
  <li>Long press any visit to <strong>Edit / Delete</strong> full patient data.</li>
</ul>

<h3>💊 Medicine Billing System</h3>
<ul>
  <li>Dynamic <strong>medicine picker dialog</strong> with search functionality.</li>
  <li>Add multiple medicines with:
    <ul>
      <li>Quantity</li>
      <li>Price per unit</li>
      <li>Discount (%)</li>
      <li>Final amount auto-calculated</li>
    </ul>
  </li>
  <li>Live medicine cart preview with deletion.</li>
  <li>Payment mode selection for medicine bill (e.g., Cash, UPI).</li>
  <li>Auto-calculated grand total.</li>
</ul>

<h3>📚 Visit History with Filters</h3>
<ul>
  <li><strong>View all visit records</strong> using a beautiful <strong>CardView</strong>-based layout.</li>
  <li>Filter visits by:
    <ul>
      <li>📅 Date Range (from/to using date pickers)</li>
      <li>🔍 Keyword search (name, age, phone, amount)</li>
    </ul>
  </li>
  <li>Fully formatted patient summary with medicine bill attached.</li>
  <li>All items shown in elegant <strong>CardView</strong> with bold, wrapped text.</li>
</ul>

<h3>🖋 Editable Records Dialog</h3>
<ul>
  <li>Long press on any visit entry to <strong>edit all data</strong>:
    <ul>
      <li>Name, Age, Date, Payment modes</li>
      <li>Medicine details, discounts, amounts</li>
    </ul>
  </li>
  <li>Dialog is rounded, scrollable, and fully styled in Material 3</li>
  <li>Auto-fills selected record for easy updates</li>
</ul>

<h3>🎨 Beautiful Material UI</h3>
<ul>
  <li>Uses <strong>Material You (Material 3)</strong> components</li>
  <li>Rounded, modern inputs and dialogs</li>
  <li>Clean color themes (light/dark supported)</li>
  <li>Responsive layout for all Android screen sizes</li>
</ul>

<h3>⚙ Intelligent UX Behavior</h3>
<ul>
  <li>Auto-update final amount when quantity or discount changes</li>
  <li>Dynamic search UI updates based on selected filter</li>
  <li>Real-time medicine cart calculation</li>
  <li>EditText validations and user-friendly errors</li>
</ul>

<h3>🔐 Data Privacy & Offline-First</h3>
<ul>
  <li>100% local data storage via <strong>SQLite</strong></li>
  <li>No internet required — suitable for rural clinics</li>
  <li>No sensitive permissions or data leaks</li>
</ul>

<hr>

<h2>📦 Tech Stack</h2>

<table>
  <thead>
    <tr><th>Technology</th><th>Usage</th></tr>
  </thead>
  <tbody>
    <tr><td>Java</td><td>Core app logic</td></tr>
    <tr><td>SQLite</td><td>Local database</td></tr>
    <tr><td>SharedPreferences</td><td>Config storage</td></tr>
    <tr><td>Lottie</td><td>Animations (optional)</td></tr>
    <tr><td>Material 3 (MDC)</td><td>UI components & theming</td></tr>
    <tr><td>BottomSheet</td><td>Medicine selection modal</td></tr>
    <tr><td>RecyclerView</td><td>Dynamic medicine list</td></tr>
    <tr><td>CardView</td><td>Visit entries</td></tr>
    <tr><td>Dialogs</td><td>Editing/Confirmation popups</td></tr>
  </tbody>
</table>

<hr>

<h2>🧪 Screenshots</h2>
<p><em>Coming soon: screenshots of Patient Entry, Visit History, Medicine Picker, and Filters.</em></p>

<hr>

<h2>🧩 Upcoming Features</h2>
<ul>
  <li>🗃 Export records to PDF / Excel</li>
  <li>☁️ Backup and Restore DB (cloud/local)</li>
  <li>📈 Analytics Dashboard for visit patterns</li>
  <li>👨‍⚕️ Multi-user login (doctor/staff)</li>
  <li>🔄 Google Drive / Firebase Sync (optional)</li>
  <li>📱 Tablet layout & RTL language support</li>
</ul>

<hr>

<h2>⚙ How to Build</h2>
<ol>
  <li>Clone or download this repository.</li>
  <li>Open in <strong>Android Studio</strong> or any Java-supported IDE.</li>
  <li>Make sure <strong>API 23+</strong> is used.</li>
  <li>Build and install APK on your device.</li>
</ol>

<hr>

<h2>👨‍🔬 Developer Info</h2>
<p><strong>👨‍💻 Author</strong>: <em>Ramm Healthcare</em><br>
<strong>📌 Purpose</strong>: Provide smart, secure, and offline-friendly clinic management for Indian doctors and general practitioners.</p>

<hr>

<blockquote><strong>Note:</strong> This app puts your clinic data in your hands. No cloud, no privacy risks. Perfect for solo or small clinic setups.</blockquote>