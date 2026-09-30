package com.operationsoft.multimap

import android.Manifest
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.operationsoft.multimap.lib.essentials.ApplyEventArgs
import com.operationsoft.multimap.lib.essentials.Tools
import com.operationsoft.multimap.lib.ui.WaypointViewActivity
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var wvWaypointEditor: WaypointViewActivity? = null
    private var btnAddWaypoint: Button? = null
    private var btnEditWaypoint: Button? = null

    private val startPoint = GeoPoint(
        50.4471871, 30.5229456)
    private var currentMarker: Marker? = null
    private var markers: MutableList<Marker?> = mutableListOf()

    private var isMarkerSelected: Boolean = false
        set(value) {
            field = value
            if (value){
                btnEditWaypoint!!.visibility = VISIBLE
            }
            else{
                btnEditWaypoint!!.visibility = GONE
            }
        }
    private var isMarkerEventOverlayEnabled: Boolean = false
    private var mapMarkerEventsOverlay: MapEventsOverlay? = null
    private var mapEventsOverlay: MapEventsOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.osmdroid.config.Configuration.getInstance()
            .load(applicationContext,
                getSharedPreferences("osmdroid_settings",
                    MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        wvWaypointEditor = findViewById(R.id.wvWaypointEditor)
        btnAddWaypoint = findViewById(R.id.btnAddWaypoint)
        btnEditWaypoint = findViewById(R.id.btnEditWaypoint)

        wvWaypointEditor!!.attachActivity(this)

        wvWaypointEditor!!.onApplied = {args -> applyParameters(args) }
        btnAddWaypoint!!.setOnClickListener(::btnAddWaypointOnClick)
        btnEditWaypoint!!.setOnClickListener(::btnEditWaypointOnClick)

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

        Tools.requestForPermissions(this, permissions)
        setMap()
    }

    private fun applyParameters(args: ApplyEventArgs){
        wvWaypointEditor!!.visibility = INVISIBLE
        btnAddWaypoint!!.visibility = VISIBLE

        mapView!!.overlays.remove(mapMarkerEventsOverlay)
        mapView!!.overlays.add(0, mapEventsOverlay)
        isMarkerEventOverlayEnabled = false

        if (currentMarker != null) {
            if (!markers.isEmpty()) {
                for (marker in markers){
                    marker!!.closeInfoWindow()
                }
            }
            currentMarker!!.closeInfoWindow()
            currentMarker!!.title = args.title
            currentMarker!!.subDescription = args.description

            currentMarker!!.setAnchor(Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM)
            currentMarker!!.icon = args.icon

            markers.add(currentMarker)
            wvWaypointEditor!!.markers = markers

            currentMarker = null

            mapView!!.invalidate()
        }
    }

    private fun btnAddWaypointOnClick(view: View?){
        currentMarker = null
        openWaypointEditor()
    }

    private fun btnEditWaypointOnClick(view: View?){
        for (i in 0..markers.size){
            if (markers[i] == currentMarker){
                markers.remove(markers[i])
                break
            }
        }
        wvWaypointEditor!!.setParameters(currentMarker!!.title, currentMarker!!.subDescription,
            currentMarker!!.icon.toBitmap())
        openWaypointEditor()
    }

    private fun openWaypointEditor() {
        isMarkerSelected = false

        btnAddWaypoint!!.visibility = INVISIBLE
        wvWaypointEditor!!.visibility = VISIBLE

        mapView!!.overlays.remove(mapEventsOverlay)
        mapView!!.overlays.add(0, mapMarkerEventsOverlay)
        isMarkerEventOverlayEnabled = true
    }

    private fun setMap() {
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setMultiTouchControls(true)
        mapView!!.setZoomLevel(19.0)

        mapView!!.controller.setCenter(startPoint)

        var mapEventsReceiver: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                addWaypointMarker(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        mapMarkerEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                isMarkerSelected = false
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView!!.overlays.add(0, mapEventsOverlay)
    }

    private fun addWaypointMarker(point: GeoPoint) {
        if (currentMarker == null) {
            currentMarker = Marker(mapView)
            currentMarker!!.setPanToView(true)
            currentMarker!!.setOnMarkerClickListener(::onMarkerClick)
        }
        else{
            mapView!!.overlays.remove(currentMarker)
        }
        currentMarker!!.position = point

        mapView!!.overlays.add(currentMarker)
        mapView!!.invalidate()
    }

    private fun onMarkerClick(clickedMarker: Marker, map: MapView) : Boolean{
        if (!isMarkerEventOverlayEnabled){
            isMarkerSelected = true
            currentMarker = clickedMarker
        }

        clickedMarker.showInfoWindow()
        mapView!!.controller.animateTo(clickedMarker.position)

        return true
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }
}
