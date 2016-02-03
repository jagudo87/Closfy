package com.agba.closfy.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.agba.closfy.R;
import com.agba.closfy.database.GestionBBDD;
import com.agba.closfy.modelo.Prenda;
import com.agba.closfy.util.Util;

import java.util.ArrayList;

public class PrendaBasicaActivity extends ActionBarActivity {

	private SQLiteDatabase db;
	private final String BD_NOMBRE = "BDClosfy";
	final GestionBBDD gestion = new GestionBBDD();

	SharedPreferences prefs;
	GridView gridview;
	int posi = -1;
	int tipoPrenda;
	ProgressDialog progDailog;
	ArrayList<Prenda> listPrendas = new ArrayList<Prenda>();

	int estilo;
	int cuentaSeleccionada;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prendas_basicas);

		// Cuenta seleccionada
		prefs = getSharedPreferences("ficheroConf", Context.MODE_PRIVATE);
		cuentaSeleccionada = Util.cuentaSeleccionada(this, prefs);

		db = openOrCreateDatabase(BD_NOMBRE, 1, null);
		if (db != null) {
			estilo = gestion.getEstiloCuenta(db, cuentaSeleccionada);
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setContentInsetsAbsolute(0, 0);
		setSupportActionBar(toolbar);

		if (estilo == 1) {
			toolbar.setBackgroundResource(R.color.azul);
		}

		// Inflate the custom view and add click handlers for the buttons
		View actionBarButtons = getLayoutInflater().inflate(R.layout.accept_cancel_actionbar,
				new LinearLayout(this), false);

		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
		cancelActionView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		View doneActionView = actionBarButtons.findViewById(R.id.action_done);
		doneActionView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getIntent().putExtra("prendaBasicaSeleccionada", posi);
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});

		// Hide the icon, title and home/up button
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		// Set the custom view and allow the bar to show it
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL;
		getSupportActionBar().setCustomView(actionBarButtons, layoutParams);

		Bundle bundle = getIntent().getExtras();
		tipoPrenda = bundle.getInt("tipoPrenda");

		gridview = (GridView) findViewById(R.id.gridPrendaBasica);

		new CargarPrendasBasicasTask().execute();

	}

	public void obtenerPrendas() {

		if (tipoPrenda == 1) {
			listPrendas = Util.obtenerPrendasBasicasSuperior(this, tipoPrenda, estilo);
		} else if (tipoPrenda == 2) {
			listPrendas = Util.obtenerPrendasBasicasInferior(this, tipoPrenda, estilo);
		} else if (tipoPrenda == 3) {
			listPrendas = Util.obtenerPrendasBasicasCuerpoEntero(this,
					tipoPrenda, estilo);
		} else if (tipoPrenda == 4) {
			listPrendas = Util.obtenerPrendasBasicasAbrigo(this, tipoPrenda, estilo);
		} else if (tipoPrenda == 5) {
			listPrendas = Util.obtenerPrendasBasicasCalzado(this, tipoPrenda, estilo);
		} else if (tipoPrenda == 6) {
			listPrendas = Util.obtenerPrendasBasicasComplemento(this,
					tipoPrenda, estilo);
		}
	}

	// Anadiendo las opciones de menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_setting, menu);
		return true;
	}

	// Anadiendo funcionalidad a las opciones de menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LayoutInflater li = LayoutInflater.from(this);
		View view = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert;
		switch (item.getItemId()) {
		case R.id.btInfo:
			view = li.inflate(R.layout.info, null);
			builder.setView(view);
			builder.setTitle(getResources().getString(R.string.informacion));
			builder.setIcon(R.drawable.ic_info_azul);
			builder.setCancelable(false);
			builder.setPositiveButton(getResources()
					.getString(R.string.aceptar),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			alert = builder.create();
			alert.show();
			return true;
		case R.id.btAcerca:
			view = li.inflate(R.layout.acerca, null);
			builder.setView(view);
			builder.setTitle(getResources().getString(R.string.app_name));
			builder.setIcon(R.drawable.icon_app);
			builder.setCancelable(false);
			builder.setPositiveButton(getResources()
					.getString(R.string.aceptar),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			alert = builder.create();
			alert.show();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class GridAdapterPrendaBasica extends BaseAdapter implements
			OnClickListener {
		private Context context;
		ArrayList<Prenda> listaPrendas = new ArrayList<Prenda>();
		View viewAnterior;

		public GridAdapterPrendaBasica(Context c, ArrayList<Prenda> listPrendas) {
			context = c;
			listaPrendas = listPrendas;
		}

		public int getCount() {
			return listaPrendas.size();
		}

		public Object getItem(int position) {
			return listaPrendas.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(
						R.layout.seleccionar_prenda_basica_adapter, parent,
						false);
			} else {
				v = (View) convertView;
			}

			LinearLayout layoutPrenda = (LinearLayout) v
					.findViewById(R.id.layoutPrenda);

			ImageView imagenPrenda = (ImageView) v
					.findViewById(R.id.imagenPrenda);

			imagenPrenda.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imagenPrenda.setPadding(5, 5, 5, 5);

			layoutPrenda.setOnClickListener(this);
			imagenPrenda.setImageDrawable(listaPrendas.get(position).getFoto());

			ImageView imagenCheck = (ImageView) v
					.findViewById(R.id.imagencheck);

			if (posi == listaPrendas.get(position).getIdPrenda()) {
				if (estilo == 1) {
					imagenCheck.setBackgroundResource(R.drawable.tic_azul);
				} else {
					imagenCheck.setBackgroundResource(R.drawable.tic);
				}
			} else {
				imagenCheck.setBackgroundResource(android.R.color.transparent);
			}

			layoutPrenda.setTag(listaPrendas.get(position).getIdPrenda());
			v.setTag(listaPrendas.get(position).getIdPrenda());

			return v;
		}

		// Al pulsar un dia del calendario
		@Override
		public void onClick(View view) {
			int position = (Integer) view.getTag();

			if (position == posi) {
				posi = -1;
			} else {
				posi = position;
			}

			notifyDataSetChanged();
		}

	}

	public class CargarPrendasBasicasTask extends
			AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDailog = new ProgressDialog(PrendaBasicaActivity.this);
			progDailog.setIndeterminate(false);
			progDailog.setMessage("Cargando ...");
			progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDailog.setCancelable(true);
			progDailog.show();
		}

		// Decode image in background.
		@Override
		protected Void doInBackground(Integer... params) {

			// Recuperamos las prendas
			obtenerPrendas();
			
			return null;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Void result) {
			final GridAdapterPrendaBasica gridadapter = new GridAdapterPrendaBasica(
					PrendaBasicaActivity.this, listPrendas);
			gridview.setAdapter(gridadapter);
			progDailog.dismiss();
		}
	}
}
