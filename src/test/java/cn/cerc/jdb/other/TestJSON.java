package cn.cerc.jdb.other;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestJSON {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_gson() {
		String v1 = "{\"val\":224389.65}";
		Gson json = new Gson();
		Map<String, Object> obj = json.fromJson(v1, new TypeToken<Map<String, Object>>() {
		}.getType());
		String v2 = json.toJson(obj);
		assertEquals(v1, v2);
	}
}
