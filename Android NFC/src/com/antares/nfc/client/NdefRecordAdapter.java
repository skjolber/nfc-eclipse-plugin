package com.antares.nfc.client;

import java.util.List;

import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.GeoRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NdefRecordAdapter extends ArrayAdapter<Record> {
	private Context context;
	private List<Record> records;
	private NdefMessageEncoder ndefMessageEncoder = NdefContext.getNdefMessageEncoder();
	
	public NdefRecordAdapter(Context context, List<Record> records) {
		super(context, R.layout.ndef_record);
		this.context = context;
		this.records = records;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Record record = records.get(position);

		View view;
		if(record instanceof AndroidApplicationRecord) {
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_aar, parent, false);
			
			if(androidApplicationRecord.hasPackageName()) {
				TextView textView = (TextView) view.findViewById(R.id.androidApplicationPackageNameValue);
				
				textView.setText(androidApplicationRecord.getPackageName());
			}
		} else if(record instanceof GeoRecord) {
			GeoRecord geoRecord = (GeoRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_geo, parent, false);
			
			if(geoRecord.hasLatitude()) {
				TextView textView = (TextView) view.findViewById(R.id.geoLatitudeValue);
				textView.setText(geoRecord.getLatitude().toString());
			} else {
				view.findViewById(R.id.geoLatitudeRow).setVisibility(View.GONE);
			}
			if(geoRecord.hasLongitude()) {
				TextView textView = (TextView) view.findViewById(R.id.geoLongitudeValue);
				textView.setText(geoRecord.getLongitude().toString());
			} else {
				view.findViewById(R.id.geoLongitudeRow).setVisibility(View.GONE);
			}
			
			if(geoRecord.hasAltitude()) {
				TextView textView = (TextView) view.findViewById(R.id.geoAltitudeValue);
				textView.setText(geoRecord.getAltitude().toString());
			} else {
				view.findViewById(R.id.geoAltitudeRow).setVisibility(View.GONE);
			}
			
			if(geoRecord.hasAddressInformation()) {
				TextView textView = (TextView) view.findViewById(R.id.geoQueryValue);
				textView.setText(geoRecord.getAddressInformation());
			} else {
				view.findViewById(R.id.geoQueryRow).setVisibility(View.GONE);
			}
		} else if(record instanceof UnsupportedExternalTypeRecord) {
			UnsupportedExternalTypeRecord externalType = (UnsupportedExternalTypeRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_external, parent, false);
			
			if(externalType.hasNamespace()) {
				TextView textView = (TextView) view.findViewById(R.id.namespaceValue);
				textView.setText(externalType.getNamespace());
			}

			if(externalType.hasContent()) {
				TextView textView = (TextView) view.findViewById(R.id.contentValue);
				textView.setText(externalType.getContent());
			}
			

		} else if(record instanceof SmartPosterRecord) {
			SmartPosterRecord smartPosterRecord = (SmartPosterRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_smartposter, parent, false);

			if(smartPosterRecord.hasTitle()) {
				TextRecord title = smartPosterRecord.getTitle();
				if(title.hasText()) {
					TextView textView = (TextView) view.findViewById(R.id.smartPosterTitleValue);
					textView.setText(title.getText());
				}
				if(title.hasLocale()) {
					TextView textView = (TextView) view.findViewById(R.id.smartPosterTitleDescriptor);
					
					String language = title.getLocale().getLanguage();
					String country = title.getLocale().getCountry();
					
					StringBuffer buffer = new StringBuffer();
					buffer.append(context.getString(R.string.smartPosterTitle));
					
					if(country != null && country.length() > 0) {
						buffer.append(" [" + language + "-" + country + "]");
					} else {
						buffer.append(" [" + language + "]");
					}

					String encoding = title.getEncoding().displayName();
					if(encoding != null && encoding.length() > 0) {
						buffer.append(" [" + encoding + "]");
					}
					
					textView.setText(buffer.toString());
				} else {
					// leave default text
				}
			}
			
			if(smartPosterRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.smartPosterURIValue);
				if(textView == null) throw new RuntimeException();
				
				UriRecord uri = smartPosterRecord.getUri();
				if(uri.hasUri()) {
					textView.setText(uri.getUri());
				}
			}

			if(smartPosterRecord.hasAction()) {
				TextView textView = (TextView) view.findViewById(R.id.smartPosterActionValue);
				textView.setText(smartPosterRecord.getAction().getAction().toString());
			}


		} else if(record instanceof TextRecord) {
			TextRecord textRecord = (TextRecord)record;

			view = inflater.inflate(R.layout.ndef_record_text, parent, false);

			if(textRecord.hasEncoding()) {
				TextView textView = (TextView) view.findViewById(R.id.textEncodingValue);
				textView.setText(textRecord.getEncoding().displayName());
			}
			if(textRecord.hasText()) {
				TextView textView = (TextView) view.findViewById(R.id.textMessageValue);
				textView.setText(textRecord.getText());
			}
			if(textRecord.hasLocale()) {
				TextView textView = (TextView) view.findViewById(R.id.textLocaleValue);
				
				String language = textRecord.getLocale().getLanguage();
				String country = textRecord.getLocale().getCountry();
				
				if(country != null && country.length() > 0) {
					textView.setText(language + "-" + country);
				} else {
					textView.setText(language);
				}				
			}
			
		} else if(record instanceof ActionRecord) {
			ActionRecord actionRecord = (ActionRecord)record;

			view = inflater.inflate(R.layout.ndef_record_action, parent, false);

			if(actionRecord.hasAction()) {
				Action action = actionRecord.getAction();
				TextView textView = (TextView) view.findViewById(R.id.actionActionValue);
				textView.setText(action.toString());
			}
			
		} else if(record instanceof MimeRecord) {
			MimeRecord mimeMediaRecord = (MimeRecord)record;

			view = inflater.inflate(R.layout.ndef_record_mimemedia, parent, false);

			if(mimeMediaRecord.hasContentType()) {
				TextView textView = (TextView) view.findViewById(R.id.mimemediaMimetypeValue);
				textView.setText(mimeMediaRecord.getContentType());
			}

			TextView textView = (TextView) view.findViewById(R.id.mimemediaSizeValue);
			textView.setText(Integer.toString(mimeMediaRecord.getContentAsBytes().length));
		} else if(record instanceof UnknownRecord) {
			view = inflater.inflate(R.layout.ndef_record_unknown, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(context.getString(R.string.unknown));

		} else if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;

			view = inflater.inflate(R.layout.ndef_record_alternative_carrier, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(context.getString(R.string.alternativeCarrier));
			
			if(alternativeCarrierRecord.hasCarrierPowerState()) {
				TextView value = (TextView) view.findViewById(R.id.alternativeCarrierCarrierPowerStateValue);
				value.setText(alternativeCarrierRecord.getCarrierPowerState().toString());
			}
			
			if(alternativeCarrierRecord.hasCarrierDataReference()) {
				TextView value = (TextView) view.findViewById(R.id.alternativeCarrierCarrierDataReferenceValue);
				value.setText(alternativeCarrierRecord.getCarrierDataReference());
			}
			
			TextView value = (TextView) view.findViewById(R.id.alternativeCarrierAuxiliaryDataReferencesValue);
			value.setText(Integer.toString(alternativeCarrierRecord.getAuxiliaryDataReferences().size()));
			
			
		} else if(record instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_carrier, parent, false);
			
			if(handoverCarrierRecord.hasCarrierTypeFormat()) {
				TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierTypeFormatValue);
				value.setText(handoverCarrierRecord.getCarrierTypeFormat().toString());
			}
			
			if(handoverCarrierRecord.hasCarrierType()) {
				TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierTypeValue);
				
				Object carrierType = handoverCarrierRecord.getCarrierType();
				if(carrierType instanceof String) {
					value.setText((String)carrierType);
				} else {
					value.setText(carrierType.getClass().getSimpleName());
				}
			}

			TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierDataValue);
			if(handoverCarrierRecord.hasCarrierData()) {
				value.setText(context.getString(R.string.handoverCarrierCarrierDataValue, handoverCarrierRecord.getCarrierDataSize()));
			} else {
				value.setText("-");
			}
		} else if(record instanceof HandoverRequestRecord) {
			HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_request, parent, false);
			
			TextView value = (TextView) view.findViewById(R.id.handoverRequestMajorVersionValue);
			value.setText(Byte.toString(handoverRequestRecord.getMajorVersion()));

			value = (TextView) view.findViewById(R.id.handoverRequestMinorVersionValue);
			value.setText(Byte.toString(handoverRequestRecord.getMinorVersion()));
			
			if(handoverRequestRecord.hasCollisionResolution()) {
				value = (TextView) view.findViewById(R.id.handoverRequestCollisionResolutionValue);
				value.setText(Integer.toString(handoverRequestRecord.getCollisionResolution().getRandomNumber()));
			}

			value = (TextView) view.findViewById(R.id.handoverRequestAlternativeCarriersValue);
			value.setText(Integer.toString(handoverRequestRecord.getAlternativeCarriers().size()));
			
		} else if(record instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_select, parent, false);
			
			TextView value = (TextView) view.findViewById(R.id.handoverSelectMajorVersionValue);
			value.setText(Byte.toString(handoverSelectRecord.getMajorVersion()));

			value = (TextView) view.findViewById(R.id.handoverSelectMinorVersionValue);
			value.setText(Byte.toString(handoverSelectRecord.getMinorVersion()));
			
			value = (TextView) view.findViewById(R.id.handoverSelectAlternativeCarriersValue);
			value.setText(Integer.toString(handoverSelectRecord.getAlternativeCarriers().size()));

			if(handoverSelectRecord.hasError()) {
				value = (TextView) view.findViewById(R.id.handoverSelectErrorValue);
				value.setText(handoverSelectRecord.getError().getErrorReason().toString());
			}

		} else if(record instanceof EmptyRecord) {
			EmptyRecord emptyRecord = (EmptyRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_empty, parent, false);
		} else if(record instanceof UriRecord) {
			UriRecord uriRecord = (UriRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_uri, parent, false);
			
			if(uriRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.uriValue);
				textView.setText(uriRecord.getUri());
			}
		} else if(record instanceof AbsoluteUriRecord) {
			AbsoluteUriRecord uriRecord = (AbsoluteUriRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_absolute_uri, parent, false);
			
			if(uriRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.uriValue);
				textView.setText(uriRecord.getUri());
			}
		} else if(record instanceof GenericControlRecord) {
			GenericControlRecord genericControlRecord = (GenericControlRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_generic_control, parent, false);

			TextView configurationByteView = (TextView) view.findViewById(R.id.genericControlConfigurationByteValue);
			configurationByteView.setText(Byte.toString(genericControlRecord.getConfigurationByte()));
			
			if(genericControlRecord.hasTarget()) {
				GcTargetRecord target = genericControlRecord.getTarget();
				if(target.hasTargetIdentifier()) {
					TextView textView = (TextView) view.findViewById(R.id.genericControlTargetValue);
					textView.setText(target.getTargetIdentifier().getClass().getSimpleName());
				}
			} else {
				View tableRow = view .findViewById(R.id.genericControlTargetRow);
				tableRow.setVisibility(View.GONE);
			}
			
			if(genericControlRecord.hasAction()) {
				TextView textView = (TextView) view.findViewById(R.id.genericControlActionValue);
				
				GcActionRecord action = genericControlRecord.getAction();
				if(action.hasAction()) {
					textView.setText(action.getAction().toString());
				}
			} else {
				View tableRow = view .findViewById(R.id.genericControlActionRow);
				tableRow.setVisibility(View.GONE);
			}

			if(genericControlRecord.hasData()) {
				GcDataRecord data = genericControlRecord.getData();
				TextView textView = (TextView) view.findViewById(R.id.genericControlDataValue);
				textView.setText(context.getString(R.string.genericControlDataRecords, data.getRecords().size()));
			} else {
				View tableRow = view .findViewById(R.id.genericControlDataRow);
				tableRow.setVisibility(View.GONE);
			}
		} else {
			view = inflater.inflate(R.layout.ndef_record, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(record.getClass().getSimpleName());
			
			// set the size
			textView = (TextView) view.findViewById(R.id.size);
			textView.setText(Integer.toString(ndefMessageEncoder.encode(record).length));
		}
		
		return view;
	}
	
	@Override
	public int getCount() {
		return records.size();
	}
}