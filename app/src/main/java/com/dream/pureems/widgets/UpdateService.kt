package com.dream.pureems.widgets

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dream.pureems.R

class UpdateService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListRemoteViewsFactory(this.applicationContext, intent)
    }

    internal inner class ListRemoteViewsFactory(
        private val mContext: Context,
        private val intent: Intent,
    ) :
        RemoteViewsFactory {
        override fun onCreate() {
        }

        override fun onDataSetChanged() {}
        override fun onDestroy() {}
        override fun getCount(): Int {
            return 1
        }

        override fun getViewAt(position: Int): RemoteViews {
            return RemoteViews(
                mContext.packageName,
                R.layout.item_widget_image
            ).apply { setImageViewBitmap(R.id.schedule_bitmap, ViewUtils.scheduleImage) }
        }

        override fun getLoadingView(): RemoteViews {
            return RemoteViews(
                mContext.packageName,
                R.layout.item_widget_image
            )
        }


        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }
}