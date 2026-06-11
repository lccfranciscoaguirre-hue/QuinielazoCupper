const express = require('express');
const router = express.Router();
const axios = require('axios');

const groups = {
    'Group A': ['Mexico', 'South Africa', 'South Korea', 'Czechia'],
    'Group B': ['Canada', 'Bosnia & Herzegovina', 'Qatar', 'Switzerland'],
    'Group C': ['Brazil', 'Morocco', 'Haiti', 'Scotland'],
    'Group D': ['United States', 'Paraguay', 'Australia', 'Türkiye'],
    'Group E': ['Germany', 'Curaçao', 'Ivory Coast', 'Ecuador'],
    'Group F': ['Netherlands', 'Japan', 'Sweden', 'Tunisia'],
    'Group G': ['Belgium', 'Egypt', 'Iran', 'New Zealand'],
    'Group H': ['Spain', 'Cape Verde', 'Saudi Arabia', 'Uruguay'],
    'Group I': ['France', 'Senegal', 'Iraq', 'Norway'],
    'Group J': ['Argentina', 'Algeria', 'Austria', 'Jordan'],
    'Group K': ['Portugal', 'DR Congo', 'Uzbekistan', 'Colombia'],
    'Group L': ['England', 'Croatia', 'Ghana', 'Panama']
};

const countries = Object.values(groups).flat().sort();

const allMatches = [];
for (const [groupName, teams] of Object.entries(groups)) {
    const matches = [
        { home: teams[0], away: teams[1] }, { home: teams[2], away: teams[3] },
        { home: teams[0], away: teams[2] }, { home: teams[1], away: teams[3] },
        { home: teams[3], away: teams[0] }, { home: teams[1], away: teams[2] }
    ];
    matches.forEach(m => allMatches.push({ ...m, group: groupName }));
}

const countryFlags = {
    'Mexico': 'mx', 'South Africa': 'za', 'South Korea': 'kr', 'Czechia': 'cz',
    'Canada': 'ca', 'Bosnia & Herzegovina': 'ba', 'Qatar': 'qa', 'Switzerland': 'ch',
    'Brazil': 'br', 'Morocco': 'ma', 'Haiti': 'ht', 'Scotland': 'gb-sct',
    'United States': 'us', 'Paraguay': 'py', 'Australia': 'au', 'Türkiye': 'tr',
    'Germany': 'de', 'Curaçao': 'cw', 'Ivory Coast': 'ci', 'Ecuador': 'ec',
    'Netherlands': 'nl', 'Japan': 'jp', 'Sweden': 'se', 'Tunisia': 'tn',
    'Belgium': 'be', 'Egypt': 'eg', 'Iran': 'ir', 'New Zealand': 'nz',
    'Spain': 'es', 'Cape Verde': 'cv', 'Saudi Arabia': 'sa', 'Uruguay': 'uy',
    'France': 'fr', 'Senegal': 'sn', 'Iraq': 'iq', 'Norway': 'no',
    'Argentina': 'ar', 'Algeria': 'dz', 'Austria': 'at', 'Jordan': 'jo',
    'Portugal': 'pt', 'DR Congo': 'cd', 'Uzbekistan': 'uz', 'Colombia': 'co',
    'England': 'gb-eng', 'Croatia': 'hr', 'Ghana': 'gh', 'Panama': 'pa'
};

let registrationEnabled = true;

function isAuthenticated(req, res, next) {
    if (req.session && req.session.isAdmin) {
        return next();
    }
    res.redirect('/login');
}

router.get('/', (req, res) => {
    res.render('home', { title: 'Mundial 2026 - Inicio', registrationEnabled });
});

router.get('/login', (req, res) => {
    res.render('login', { title: 'Acceso Administrador', error: null });
});

router.post('/login', async (req, res) => {
    const { username, password } = req.body;
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.post(`${backendUrl}/api/auth/login`, { username, password });
        if (response.data.success) {
            req.session.isAdmin = true;
            res.redirect('/admin');
        } else {
            res.render('login', { title: 'Acceso Administrador', error: 'Usuario o contraseña incorrectos' });
        }
    } catch (error) {
        console.error('Error conectando con el backend:', error.message);
        res.render('login', { title: 'Acceso Administrador', error: 'Error de conexión con el servidor de autenticación' });
    }
});

router.get('/registrar', (req, res) => {
    res.redirect('/quinielazo');
});

router.get('/quinielazo', (req, res) => {
    if (!registrationEnabled) {
        return res.send('El registro está deshabilitado actualmente por el administrador.');
    }
    res.render('quinielazo', { title: 'Tu Quinielazo 2026', groups, allMatches, countryFlags, countries });
});

router.get('/estadisticas', async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.get(`${backendUrl}/api/participantes`);
        const participantsFromDb = response.data;
        const sortedParticipants = [...participantsFromDb].sort((a, b) => b.puntos - a.puntos);
        res.render('estadisticas', { title: 'Ranking de Participantes', participants: sortedParticipants, countryFlags });
    } catch (error) {
        console.error('Error al obtener participantes para estadísticas:', error.message);
        res.render('estadisticas', { title: 'Ranking de Participantes', participants: [], countryFlags });
    }
});

router.get('/concentrado', async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const [participantesRes, prediccionesRes, partidosRes] = await Promise.all([
            axios.get(`${backendUrl}/api/participantes`),
            axios.get(`${backendUrl}/api/predicciones`),
            axios.get(`${backendUrl}/api/partidos`)
        ]);
        res.render('concentrado', { 
            title: 'Concentrado de Pronósticos', 
            participantes: participantesRes.data,
            predicciones: prediccionesRes.data,
            partidos: partidosRes.data, // Pasamos los partidos con resultados oficiales
            countryFlags
        });
    } catch (error) {
        console.error('Error al obtener datos para concentrado:', error.message);
        res.render('concentrado', { title: 'Concentrado de Pronósticos', participantes: [], predicciones: [], partidos: [], countryFlags });
    }
});

router.get('/admin', isAuthenticated, async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const [partidosRes, participantesRes] = await Promise.all([
            axios.get(`${backendUrl}/api/partidos`),
            axios.get(`${backendUrl}/api/participantes`)
        ]);
        
        let partidos = partidosRes.data;
        partidos.sort((a, b) => a.grupo.localeCompare(b.grupo));
        
        res.render('admin', { 
            title: 'Panel de Administrador', 
            groups, 
            allMatches: partidos, 
            participantes: participantesRes.data,
            countryFlags, 
            registrationEnabled 
        });
    } catch (error) {
        console.error('Error al obtener datos para admin:', error.message);
        res.render('admin', { title: 'Panel de Administrador', groups, allMatches: [], participantes: [], countryFlags, registrationEnabled });
    }
});

router.get('/logout', (req, res) => {
    req.session.destroy();
    res.redirect('/');
});

router.post('/api/toggle-registration', (req, res) => {
    registrationEnabled = req.body.enabled;
    res.json({ success: true, enabled: registrationEnabled });
});

router.get('/api/descargar-reporte', isAuthenticated, async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.get(`${backendUrl}/api/admin/descargar-reporte`, {
            responseType: 'arraybuffer'
        });
        res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        res.setHeader('Content-Disposition', 'attachment; filename=Reporte_Quinielazo.xlsx');
        res.send(response.data);
    } catch (error) {
        console.error('Error al descargar el reporte del backend:', error.message);
        res.status(500).send('Error al descargar el archivo');
    }
});

router.post('/api/actualizar-resultados', async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.post(`${backendUrl}/api/partidos/actualizar-resultados`, req.body);
        res.json(response.data);
    } catch (error) {
        console.error('Error al actualizar resultados en el backend:', error.message);
        res.status(500).json({ success: false, message: 'Error al conectar con el servidor de puntuación' });
    }
});

router.post('/api/registro-completo', async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.post(`${backendUrl}/api/predicciones/registro-completo`, req.body);
        res.json(response.data);
    } catch (error) {
        let errorMessage = 'Error desconocido';
        if (error.response && error.response.data) {
            // El servidor respondió con un código de estado fuera del rango 2xx
            errorMessage = typeof error.response.data === 'string' 
                ? error.response.data 
                : (error.response.data.message || JSON.stringify(error.response.data));
        } else if (error.request) {
            // La petición fue hecha pero no se recibió respuesta
            errorMessage = 'No se recibió respuesta del servidor (timeout o servidor caído)';
        } else {
            // Algo sucedió al configurar la petición que provocó un error
            errorMessage = error.message;
        }
        console.error('Error al guardar registro y predicciones:', errorMessage);
        res.status(500).json({ success: false, message: `Error desde el backend: ${errorMessage}` });
    }
});

router.post('/api/borrar-bd', isAuthenticated, async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    try {
        const response = await axios.post(`${backendUrl}/api/admin/borrar-bd`, req.body);
        res.json(response.data);
    } catch (error) {
        console.error('Error al intentar borrar base de datos:', error.message);
        res.status(500).json({ success: false, message: 'Error de conexión con el backend.' });
    }
});

router.post('/api/eliminar-participante', isAuthenticated, async (req, res) => {
    const backendUrl = req.app.locals.backendUrl;
    const { id, password } = req.body;
    try {
        const response = await axios.delete(`${backendUrl}/api/admin/eliminar-participante/${id}`, {
            data: { password }
        });
        res.json(response.data);
    } catch (error) {
        console.error('Error al intentar eliminar participante:', error.message);
        res.status(500).json({ success: false, message: 'Error de conexión con el backend.' });
    }
});

module.exports = router;