package com.example.cuchareableapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variables principales
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocationMarker: Marker? = null

    // Código de request para permisos
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar el callback para actualizaciones de ubicación
        setupLocationCallback()

        // Obtener el fragmento del mapa y configurarlo
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Configura el callback que se ejecuta cuando hay actualizaciones de ubicación
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateUserLocationOnMap(location)
                }
            }
        }
    }

    /**
     * Se ejecuta cuando el mapa está listo para usar
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar el mapa para mostrar la ubicación de Chimbote, Perú
        val chimboteLocation = LatLng(-9.0853, -78.5694)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chimboteLocation, 13f))

        // Agregar marcadores de lugares de comida
        addFoodMarkers()

        // Configurar el listener para clicks en marcadores
        setupMarkerClickListener()

        // Verificar y solicitar permisos de ubicación
        checkLocationPermission()
    }

    /**
     * Agrega los marcadores fijos de lugares de comida callejera
     */
    private fun addFoodMarkers() {
        // Marcador 1: Anticuchos Don Pepe
        val anticuchos = LatLng(-9.0853, -78.5694)
        mMap.addMarker(
            MarkerOptions()
                .position(anticuchos)
                .title("Anticuchos Don Pepe")
                .snippet("Tipo: Anticuchos y Parrillas")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )

        // Marcador 2: Hamburguesas Ricky
        val hamburguesas = LatLng(-9.0830, -78.5750)
        mMap.addMarker(
            MarkerOptions()
                .position(hamburguesas)
                .title("Hamburguesas Ricky")
                .snippet("Tipo: Hamburguesas y Comida Rápida")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        )

        // Marcador 3: Chochos del Parque
        val chochos = LatLng(-9.0877, -78.5720)
        mMap.addMarker(
            MarkerOptions()
                .position(chochos)
                .title("Chochos del Parque")
                .snippet("Tipo: Chochos y Snacks")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    /**
     * Configura el listener para cuando se hace click en un marcador
     */
    private fun setupMarkerClickListener() {
        mMap.setOnMarkerClickListener { marker ->
            // Verificar si es el marcador del usuario actual
            if (marker == currentLocationMarker) {
                Toast.makeText(this, "Esta es tu ubicación actual", Toast.LENGTH_SHORT).show()
                return@setOnMarkerClickListener true
            }

            // Mostrar información del marcador en un AlertDialog
            showMarkerInfo(marker)
            true
        }
    }

    /**
     * Muestra un AlertDialog con la información del marcador seleccionado
     */
    private fun showMarkerInfo(marker: Marker) {
        val position = marker.position
        val message = """
            📍 Lugar: ${marker.title}
            🍴 ${marker.snippet}
            📍 Latitud: ${String.format("%.6f", position.latitude)}
            📍 Longitud: ${String.format("%.6f", position.longitude)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Información del Lugar")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Ir al lugar") { _, _ ->
                Toast.makeText(this, "Función de navegación próximamente", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Verifica si tenemos permisos de ubicación y los solicita si es necesario
     */
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Tenemos permisos, iniciar seguimiento de ubicación
            startLocationUpdates()
        } else {
            // Solicitar permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Maneja la respuesta del usuario a la solicitud de permisos
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permisos concedidos, iniciar seguimiento
                    startLocationUpdates()
                } else {
                    // Permisos denegados
                    Toast.makeText(
                        this,
                        "Los permisos de ubicación son necesarios para mostrar tu posición",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Inicia las actualizaciones de ubicación en tiempo real
     */
    private fun startLocationUpdates() {
        // Verificar permisos nuevamente por seguridad
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Configurar las opciones de solicitud de ubicación
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Actualizar cada 5 segundos
            fastestInterval = 2000 // Intervalo más rápido: 2 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Iniciar las actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )

        // Habilitar el botón "Mi ubicación" en el mapa
        mMap.isMyLocationEnabled = true
    }

    /**
     * Actualiza la ubicación del usuario en el mapa con un marcador personalizado
     */
    private fun updateUserLocationOnMap(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)

        // Remover el marcador anterior si existe
        currentLocationMarker?.remove()

        // Agregar nuevo marcador de ubicación del usuario con ícono rojo
        currentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(userLocation)
                .title("Tu ubicación")
                .snippet("Ubicación actual en tiempo real")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Solo mover la cámara la primera vez para no ser intrusivo
        if (currentLocationMarker != null) {
            // Comentar la siguiente línea si no quieres que la cámara siga al usuario
            // mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation))
        }
    }

    /**
     * Detener las actualizaciones de ubicación cuando la actividad se pausa
     */
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Reanudar las actualizaciones cuando la actividad se reanuda
     */
    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }
}