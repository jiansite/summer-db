package cn.cerc.jdb.field;

@Deprecated
public class BooleanDefine extends AbstractDefine {
	public BooleanDefine() {
		this.setWidth(2);
	}

	@Override
	public boolean validate(Object object) {
		return object == null || object.getClass().equals(boolean.class) || object.getClass().equals(Boolean.class);
	}
}
