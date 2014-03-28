package com.willwinder.universalgcodesender.pendantui;

import java.util.ArrayList;
import java.util.List;

public class PendantConfigBean {
	private List<StepSizeOption> stepSizeList = new ArrayList<>();
	private List<ShortCutButton> shortCutButtonList = new ArrayList<>();
	
	public static class StepSizeOption{
		private String value;
		private String label;
		private boolean selected;
		public StepSizeOption(){}
		public StepSizeOption(String value, String label, boolean selected) {
			super();
			this.value = value;
			this.label = label;
			this.selected = selected;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		@Override
		public String toString() {
			return "StepSizeOption [value=" + value + ", label=" + label + ", selected=" + selected + "]";
		}
	}
	
	public static class ShortCutButton{
		private String label;
		private List<String> gCodeCommandList = new ArrayList<>();
		public ShortCutButton(){}
		public ShortCutButton(String label, String gCodeCommand) {
			super();
			this.label = label;
			this.gCodeCommandList.add(gCodeCommand);
		}
		
		public ShortCutButton(String label, List<String> gCodeCommandList) {
			super();
			this.label = label;
			this.gCodeCommandList = gCodeCommandList;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public List<String> getgCodeCommandList() {
			return gCodeCommandList;
		}
		public void setgCodeCommandList(List<String> gCodeCommandList) {
			this.gCodeCommandList = gCodeCommandList;
		}
		@Override
		public String toString() {
			return "ShortCutButton [label=" + label + ", gCodeCommandList=" + gCodeCommandList + "]";
		}

	}
	public PendantConfigBean() {
	}
	public List<StepSizeOption> getStepSizeList() {
		return stepSizeList;
	}
	public void setStepSizeList(List<StepSizeOption> stepSizeList) {
		this.stepSizeList = stepSizeList;
	}
	public List<ShortCutButton> getShortCutButtonList() {
		return shortCutButtonList;
	}
	public void setShortCutButtonList(List<ShortCutButton> shortCutButtonList) {
		this.shortCutButtonList = shortCutButtonList;
	}
	@Override
	public String toString() {
		return "PendantConfigBean [stepSizeList=" + stepSizeList + ", shortCutButtonList=" + shortCutButtonList + "]";
	}
}
