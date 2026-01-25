package com.jdailer.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QuickSettingsTileService : TileService() {
    override fun onClick() {
        super.onClick()
        qsTile?.state = if (qsTile?.state == Tile.STATE_ACTIVE) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }
        qsTile?.updateTile()
    }
}
