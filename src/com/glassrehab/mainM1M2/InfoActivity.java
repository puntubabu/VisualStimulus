package com.glassrehab.mainM1M2;

import java.util.ArrayList;
import java.util.List;

import org.hitlabnz.helloworld.R;

import android.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

/**
 * The about cards for the app.
 *
 */
public class InfoActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CardScrollView cardScrollView = new CardScrollView(this);
        cardScrollView.setAdapter(new InfoCardScrollAdapter(this));
        cardScrollView.activate();
        setContentView(cardScrollView);
	}
	

	/**
	 * A View that shows horizontally scrolling children views, referred to as cards. 
	 * The cards come from the CardScrollAdapter that is associated with the CardScrollView.
	 * Each card visually represents a certain Object item.
	 */
	public class InfoCardScrollAdapter extends CardScrollAdapter {
		
		/**
		 * The list of information cards.
		 */
		private List<Card> infoCards;
		
		public InfoCardScrollAdapter(Context context) {
			infoCards = new ArrayList<Card>();
			
			Card card = new Card(context);
			card.setText("The Glass Rehab glassware is made for the stroke rehablitation at Wexner Medical Center.");
			card.setFootnote("Empowered by the KPTT project team 2014");
			infoCards.add(card);
			
			card = new Card(context);
			card.setImageLayout(ImageLayout.FULL);
			card.addImage(R.drawable.visual_rehab);
			infoCards.add(card);
			
			card = new Card(context);
			card.setImageLayout(ImageLayout.FULL);
			card.addImage(R.drawable.department);
			infoCards.add(card);
		}

		/**
		 * Return the position of the item with the given key id (which is not applicable here.)
		 */
		@Override
		public int getPosition(Object id) {
			return -1;
		}

		/**
		 * Retrieve the position of the selected item.
		 *
		 * @param item The item whose location will be found among the infocards.
		 * @return the position of the given item (card)
		 */
		public int getSelectedItemPosition(Object item) {
			return infoCards.indexOf(item);
		}

		/**
		 * Return the number of cards.
		 */
		@Override
		public int getCount() {
			return infoCards.size();
		}


		/**
		 * Retrieve the card at the given position.
		 *
		 * @param position The position form which to retrieve the object.
		 */
		@Override
		public Object getItem(int position) {
			return infoCards.get(position); // return the item (card) at given position
		}

		/**
		 * Return the view at the given position.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parentView) {
			return infoCards.get(position).getView();
		}
		
	}

}
