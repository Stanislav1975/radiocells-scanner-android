/**
 * Loads reference wifis asynchronously.
 * Upon completion callback mListener in activity is invoked.
 */
package org.openbmap.utils;

import java.util.ArrayList;

import org.mapsforge.core.model.GeoPoint;
import org.openbmap.Preferences;
import org.openbmap.activity.MapViewActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


public class WifiCatalogMapObjectsLoader extends AsyncTask<Object, Void, ArrayList<GeoPoint>> {

	private static final String	TAG	= WifiCatalogMapObjectsLoader.class.getSimpleName();

	/**
	 * Interface for activity.
	 */
	public interface OnCatalogLoadedListener {
		void onCatalogLoaded(ArrayList<GeoPoint> points);
	}

	/**
	 * Indices for doInBackground arguments
	 */
	private static final int	MIN_LAT_COL	= 0;
	private static final int	MAX_LAT_COL	= 1;
	private static final int	MIN_LON_COL	= 2;
	private static final int	MAX_LON_COL	= 3;
	
	/**
	 * Maximum reference items drawn at a time
	 * Prevents out of memory issues
	 */
	private static final int MAX_REFS = 2500;

	/**
	 * Keeps the SharedPreferences.
	 */
	private SharedPreferences prefs = null;

	/**
	 * Database containing well-known wifis from openbmap.org.
	 */
	private SQLiteDatabase mRefdb;

	private OnCatalogLoadedListener mListener;

	public WifiCatalogMapObjectsLoader(final Context context) {

		// dialog = new ProgressDialog(mContext);
		// get shared preferences
		if (context instanceof MapViewActivity) {
			setOnCatalogLoadedListener((OnCatalogLoadedListener) context);
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public final void setOnCatalogLoadedListener(final OnCatalogLoadedListener listener) {
		this.mListener = listener;
	}

	@Override
	protected final void onPreExecute() {

	}

	// TODO change signature to left, right, top, bottom
	/**
	 * Queries reference database for all wifis in specified range around map center.
	 * @param args
	 * 			Args is an object array containing
	 * 			args[0]: min latitude as double
	 * 			args[1]: max latitude as double
	 * 			args[2]: min longitude as double
	 *			args[3]: max longitude as double
	 */
	@Override
	protected final ArrayList<GeoPoint> doInBackground(final Object... args) {         

		ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

		try {
			// skipping if no reference database set 
			if (prefs.getString(Preferences.KEY_WIFI_CATALOG, Preferences.VAL_WIFI_CATALOG_NONE).equals(Preferences.VAL_WIFI_CATALOG_NONE)) {
				return points;
			}
			
			// Open ref database
			String path = Environment.getExternalStorageDirectory().getPath()
					+ prefs.getString(Preferences.KEY_DATA_DIR, Preferences.VAL_DATA_DIR)
					+ Preferences.WIFI_CATALOG_SUBDIR + "/" + prefs.getString(Preferences.KEY_WIFI_CATALOG, Preferences.VAL_REF_DATABASE);
			mRefdb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

			Cursor refs = mRefdb.rawQuery("SELECT _id, latitude, longitude FROM wifi_zone WHERE "
					+ "(latitude > ? AND latitude < ? AND longitude > ? AND longitude < ?)", 
					// TODO this probably fails around 0 meridian
					new String[] {
							String.valueOf((Double) args[MIN_LAT_COL]),
							String.valueOf((Double) args[MAX_LAT_COL]),
							String.valueOf((Double) args[MIN_LON_COL]),
							String.valueOf((Double) args[MAX_LON_COL])
					}
					);

			int i = 0;
			int latCol = refs.getColumnIndex("latitude");
			int lonCol = refs.getColumnIndex("longitude");
			while (refs.moveToNext() && i < MAX_REFS) {
				points.add(new GeoPoint(refs.getDouble(latCol), refs.getDouble(lonCol)));
				i++;
			}
			/* Log.i(TAG, i + " reference wifis received in bounding box" 
					+ "[lat min " + (Double) args[MIN_LAT_COL] + " lat max " + (Double) args[MAX_LAT_COL] + " , lon min " + (Double) args[MIN_LON_COL] + " lon max " + (Double) args[MAX_LON_COL] +"]");*/
		} catch (SQLiteException e) {
			Log.e(TAG, "Sql exception occured: " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mRefdb != null) {
				mRefdb.close();	
			}
		}

		return points;
	}

	@Override
	protected final void onPostExecute(final ArrayList<GeoPoint> points) {

		if (mListener != null) {
			mListener.onCatalogLoaded(points);
		}
	}
}