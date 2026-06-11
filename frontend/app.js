const express = require('express');
const path = require('path');
const session = require('express-session');
const cors = require('cors'); // Asegurarnos de usar cors si es necesario
const app = express();
const port = process.env.PORT || 3000;

// Configuración de la URL del Backend (Railway vs Localhost)
// Si la variable BACKEND_URL existe (en la nube), la usa. Si no, usa localhost para desarrollo local.
app.locals.backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';

// View engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

// Middleware
app.use(cors()); // Para evitar problemas de CORS si se despliegan en dominios distintos
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(path.join(__dirname, 'public')));
app.use(session({
  secret: process.env.SESSION_SECRET || 'quiniela-secret-key',
  resave: false,
  saveUninitialized: true,
  cookie: { secure: false } // En Vercel a veces esto da problemas si no está detrás de un proxy confiable. Lo dejamos en false por seguridad.
}));

// Routes
const indexRouter = require('./routes/index');
app.use('/', indexRouter);

// Error handling
app.use((req, res, next) => {
  res.status(404).send('Página no encontrada');
});

app.listen(port, () => {
  console.log(`Servidor de la Quiniela 2026 corriendo en http://localhost:${port}`);
  console.log(`Conectado al Backend en: ${app.locals.backendUrl}`);
});