package com.tom.cpl.gui.elements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.util.TabFocusHandler.Focusable;
import com.tom.cpl.math.Box;
import com.tom.cpm.externals.com.udojava.evalex.Expression.ExpressionException;
import com.tom.cpm.shared.util.ExpressionExt;

public class Spinner extends GuiElement implements Focusable {
	private float value;
	private int dp = 3;
	private List<Runnable> changeListeners = new ArrayList<>();
	private TextField txtf;
	private boolean txtfNeedsUpdate;
	private String error, lastValue;

	public Spinner(IGui gui) {
		super(gui);
		txtf = new TextField(gui);
		txtf.setEventListener(this::updateTxtf);
		txtf.setText(lastValue = roundValue(dp, value));
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x + 1, bounds.y + 1, bounds.w - 2, bounds.h - 2, enabled ? gui.getColors().button_fill : gui.getColors().button_disabled);
		txtf.draw(event, partialTicks);
		Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, 9, bounds.h / 2);
		Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, 9, bounds.h / 2);
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + 1, 8, 8, enabled ? event.isHovered(bUp) ? 16 : 8 : 0, 0, "editor");
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, 8, 8, enabled ? event.isHovered(bDown) ? 16 : 8 : 0, 8, "editor");
		if(txtfNeedsUpdate && !txtf.isFocused()) {
			txtfNeedsUpdate = false;
			txtf.setText(lastValue = roundValue(dp, value));
		}
		if(event.isHovered(bounds) && txtf.isFocused() && error != null) {
			new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.exp_error", error)).set();
		}
		if(txtf.isFocused() && error != null) {
			gui.drawRectangle(bounds.x, bounds.y, bounds.w, bounds.h, 0xffff0000);
		}
	}

	@Override
	public void mouseClick(MouseEvent e) {
		if(bounds.isInBounds(e.x, e.y) && enabled) {
			Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, bounds.w, bounds.h / 2);
			Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, bounds.w, bounds.h / 2);
			float v = gui.isAltDown() && dp > 1 ? (gui.isShiftDown() && dp > 2 ? 0.001f : 0.01f) : (gui.isShiftDown() && dp > 0 ? 0.1f : (gui.isCtrlDown() ? (gui.isShiftDown() ? 100f : 10f) : 1f));
			if(!e.isConsumed()) {
				if(bUp.isInBounds(e.x, e.y)) {
					value += v;
					changeListeners.forEach(Runnable::run);
					txtf.setText(lastValue = roundValue(dp, value));
					e.consume();
				} else if(bDown.isInBounds(e.x, e.y)) {
					value -= v;
					changeListeners.forEach(Runnable::run);
					txtf.setText(lastValue = roundValue(dp, value));
					e.consume();
				}
			}
		}
		txtf.mouseClick(e);
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		txtf.setBounds(new Box(bounds.x, bounds.y, bounds.w - 10, bounds.h));
		return super.setBounds(bounds);
	}

	@Override
	public void keyPressed(KeyboardEvent evt) {
		txtf.keyPressed(evt);
	}

	public void addChangeListener(Runnable r) {
		changeListeners.add(r);
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
		if(!txtf.isFocused())
			txtf.setText(lastValue = roundValue(dp, value));
		else txtfNeedsUpdate = true;
		error = null;
	}

	public void setDp(int dp) {
		this.dp = dp;
		txtf.setText(lastValue = roundValue(dp, value));
	}

	@Override
	public void setEnabled(boolean enabled) {
		txtf.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	private void updateTxtf() {
		if(lastValue.equals(txtf.getText()))return;
		try {
			float value = new ExpressionExt(txtf.getText().replace(',', '.').replace(';', ',')).eval();
			this.value = value;
			changeListeners.forEach(Runnable::run);
			error = null;
			txtfNeedsUpdate = true;
		} catch (ExpressionException e) {
			error = e.getMessage();
		} catch (Exception e) {
			error = gui.i18nFormat("error.cpm.unknownError");
		}
	}

	@Override
	public boolean isFocused() {
		return txtf.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		txtf.setFocused(focused);
		if(focused && enabled)txtf.setSelectionPos(0, txtf.getText().length());
		else txtf.setSelectionPos(0, 0);
	}

	public void setBackgroundColor(int bgColor) {
		txtf.setBackgroundColor(bgColor);
	}

	@Override
	public boolean isSelectable() {
		return visible && enabled;
	}

	private String roundValue(int dp, float newValue) {
		return new BigDecimal(value).setScale(dp, RoundingMode.HALF_UP).toPlainString();
	};
}