package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dummies.DummyConfigurableOfTreeLeaf;
import br.com.pedroxsqueiroz.bt.crypto.dummies.DummyConfigurableOfTreeNode;
import br.com.pedroxsqueiroz.bt.crypto.dummies.DummyConfigurableOfTreeRoot;
import br.com.pedroxsqueiroz.bt.crypto.dummies.DummyConfigurableWithDifferentTypesOfParams;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigParamUtilsTest {

	private static final double DELTA = 1e-4;
	
	@Autowired
	private ConfigurableParamsUtils configurableParamUtils;
	
	public Map<String, Object> getParameters()
	{
		return new HashMap<String, Object>(){{
			put("intParam", 1);
			put("longParam", 2);
			put("stringParam", "dummyStringParam");
			put("floatParam", "3.5");
			put("doubleParam", "4.6");
			put("integerList", new ArrayList<Object>() {{
				add(1);
				add(4);
				add("5");
				add(7);
			}});
			put("doublesList", new ArrayList<Object>() {{
				add(3);
				add(9);
				add("7");
				add(7.8F);
				add(4.5D);
			}});
			put("innerConfigurableParam", new HashMap<String, Object>() {{
				
				put("name", "dummy");
				put("params", new HashMap<String, Object>() {{
					put("intParam", "5");
					put("longParam", "6");
					put("stringParam", "innerDummyStringParam");
					put("floatParam", 7.3);
					put("doubleParam", "8.2");
				}});
			
			}});
			put("innerConfigurableParamsList", new ArrayList< Map< String, Object > > () {{
				
				add(new HashMap<String, Object>() {{
					put("name", "innerConfigurableOnList1");
					put("params", new HashMap<String, Object>() {{
						put("intParam", "9");
						put("longParam", "10");
						put("stringParam", "innerDummyStringParamOnList");
						put("floatParam", "11.1");
						put("doubleParam", 12.6);
					}});
				}});
				
				add(new HashMap<String, Object>() {{
					put("name", "innerConfigurableOnList2");
					put("params", new HashMap<String, Object>() {{
						put("intParam", "13");
						put("longParam", "14");
						put("stringParam", "innerDummyStringParamOnList1");
						put("floatParam", "15.8");
						put("doubleParam", "16.1");
					}});
				}});
				
				add(new HashMap<String, Object>() {{
					put("name", "innerConfigurableOnList3");
					put("params", new HashMap<String, Object>() {{
						put("innerConfigurableParamsList", 
								new ArrayList< Map< String, Object > > () {{
							
									add( 
										new HashMap<String, Object>() {{
											put("name", "thirdLevelConfigurableTreeNode");
											put("params", new HashMap<String, Object>() {{
												
												put("intParam", "17");
												put("longParam", "18");
												put("stringParam", "stringParamFromThirdLevelConfigurableTreeNode");
												put("floatParam", 19);
												put("doubleParam", 20);
												
											}});
										}} 
									);
							
						}});
					}});
				}});
				
			}});
		}};
	}
	
	@Test
	public void shouldExtractRawValuesOfMapOfVariousTypes() 
	{
		
		DummyConfigurableWithDifferentTypesOfParams dummyConfigurable = new DummyConfigurableWithDifferentTypesOfParams();
		
		Map<String, Object> extractedConfigParams = this.configurableParamUtils.extractConfigParamRawValuesMap(this.getParameters(), dummyConfigurable);
		
		assertEquals(1, extractedConfigParams.get("intParam"));
		assertEquals(2L, extractedConfigParams.get("longParam"));
		assertEquals("dummyStringParam", extractedConfigParams.get("stringParam"));
		assertEquals(3.5F, extractedConfigParams.get("floatParam"));
		assertEquals(4.6D, extractedConfigParams.get("doubleParam"));
		
		//-----------------------------------------------------------------------------------------
		//CHECKING INNER CONFIGURABLE
		//-----------------------------------------------------------------------------------------
		
		Object extractediInnerConfigurableObject = extractedConfigParams.get("innerConfigurableParam");
		assertEquals( ConfigurableDto.class, extractediInnerConfigurableObject.getClass() );
		
		ConfigurableDto extractediInnerConfigurable = (ConfigurableDto) extractediInnerConfigurableObject;
		Map<String, Object> extractediInnerConfigurableParams = extractediInnerConfigurable.getParams();
		
		assertEquals(5, extractediInnerConfigurableParams.get("intParam"));
		assertEquals(6L, extractediInnerConfigurableParams.get("longParam"));
		assertEquals("innerDummyStringParam", extractediInnerConfigurableParams.get("stringParam"));
		assertEquals(7.3F, extractediInnerConfigurableParams.get("floatParam"));
		assertEquals(8.2D, extractediInnerConfigurableParams.get("doubleParam"));
		
		//-----------------------------------------------------------------------------------------
		//END CHECKING INNER CONFIGURABLE
		//-----------------------------------------------------------------------------------------
		
		//-----------------------------------------------------------------------------------------
		//CHECKING SIMPLE INTEGER DATA LIST
		//-----------------------------------------------------------------------------------------

		Object integerListObject = extractedConfigParams.get("integerList");
		assertTrue(List.class.isAssignableFrom(integerListObject.getClass()));
		
		List<Integer> integersList = (List<Integer>) integerListObject;
		assertEquals( 1, integersList.get(0) );
		assertEquals( 4, integersList.get(1) );
		assertEquals( 5, integersList.get(2) );
		assertEquals( 7, integersList.get(3) );
		
		//-----------------------------------------------------------------------------------------
		//END CHECKING SIMPLE DATA LIST
		//-----------------------------------------------------------------------------------------
		
		//-----------------------------------------------------------------------------------------
		//CHECKING SIMPLE DOUBLE DATA LIST
		//-----------------------------------------------------------------------------------------

		Object doubleListObject = extractedConfigParams.get("doublesList");
		assertTrue(List.class.isAssignableFrom(doubleListObject.getClass()));
		
		List<Double> doubleList = (List<Double>) doubleListObject;
		assertEquals( 3D, doubleList.get(0), DELTA );
		assertEquals( 9D, doubleList.get(1), DELTA );
		assertEquals( 7D, doubleList.get(2), DELTA );
		assertEquals( 7.8D, doubleList.get(3), DELTA );
		assertEquals( 4.5D, doubleList.get(4), DELTA );
		
		//-----------------------------------------------------------------------------------------
		//END CHECKING SIMPLE DATA LIST
		//-----------------------------------------------------------------------------------------

		
		
		//-----------------------------------------------------------------------------------------
		//CHECKING LIST OF INNER CONFIGURABLES
		//-----------------------------------------------------------------------------------------
				
		Object extractediInnerConfigurableObjectListObject = extractedConfigParams.get("innerConfigurableParamsList");
		assertTrue( List.class.isAssignableFrom( extractediInnerConfigurableObjectListObject.getClass() )  );
		List<ConfigurableDto> extractediInnerConfigurableObjectList = (List<ConfigurableDto>) extractediInnerConfigurableObjectListObject;
		assertEquals( 3, extractediInnerConfigurableObjectList.size() );
		
		// CHECKING FIRST ITEM
		ConfigurableDto firstConfigurableItem = extractediInnerConfigurableObjectList.get(0);
		Map<String, Object> firstConfigurableItemParams = firstConfigurableItem.getParams();
		
		assertEquals(9, firstConfigurableItemParams.get("intParam"));
		assertEquals(10L, firstConfigurableItemParams.get("longParam"));
		assertEquals("innerDummyStringParamOnList", firstConfigurableItemParams.get("stringParam"));
		assertEquals(11.1F, firstConfigurableItemParams.get("floatParam"));
		assertEquals(12.6D, firstConfigurableItemParams.get("doubleParam"));

		// CHECKING SECOND ITEM
		ConfigurableDto secondConfigurableItem = extractediInnerConfigurableObjectList.get(1);
		Map<String, Object> secondConfigurableItemParams = secondConfigurableItem.getParams();
		
		assertEquals(13, secondConfigurableItemParams.get("intParam"));
		assertEquals(14L, secondConfigurableItemParams.get("longParam"));
		assertEquals("innerDummyStringParamOnList1", secondConfigurableItemParams.get("stringParam"));
		assertEquals(15.8F, secondConfigurableItemParams.get("floatParam"));
		assertEquals(16.1D, secondConfigurableItemParams.get("doubleParam"));
		
		// CHECKING THIRD ITEM
		ConfigurableDto thirdConfigurableItem = extractediInnerConfigurableObjectList.get(2);
		Map<String, Object> thirdConfigurableItemParams = thirdConfigurableItem.getParams();
		Object thirdItemInnerListObject = thirdConfigurableItemParams.get("innerConfigurableParamsList");
		
		assertTrue( List.class.isAssignableFrom(thirdItemInnerListObject.getClass()) );
		List<ConfigurableDto> thirdItemInnerList = (List<ConfigurableDto>) thirdItemInnerListObject;
		assertEquals(1, thirdItemInnerList.size());
		
		ConfigurableDto itemFromThirdLevel = thirdItemInnerList.get(0);
		Map<String, Object> itemFromThirdLevelParams = itemFromThirdLevel.getParams();
		
		assertEquals(17, itemFromThirdLevelParams.get("intParam"));
		assertEquals(18L, itemFromThirdLevelParams.get("longParam"));
		assertEquals("stringParamFromThirdLevelConfigurableTreeNode", itemFromThirdLevelParams.get("stringParam"));
		assertEquals(19F, itemFromThirdLevelParams.get("floatParam"));
		assertEquals(20D, itemFromThirdLevelParams.get("doubleParam"));
		
		//-----------------------------------------------------------------------------------------
		//END CHECKING LIST OF INNER CONFIGURABLES
		//-----------------------------------------------------------------------------------------
		
	}
	
	@Test
	public void shouldBuildTreeOfConfigurables() 
	{
		DummyConfigurableOfTreeRoot root = new DummyConfigurableOfTreeRoot();
		DummyConfigurableOfTreeNode node = new DummyConfigurableOfTreeNode();
		DummyConfigurableOfTreeLeaf leaf = new DummyConfigurableOfTreeLeaf();
		
		root.setRootParam(2);
		node.setNodeParam(5);
		
		root.setNode(node);
		node.setLeaf(leaf);
		
		this.configurableParamUtils.resolveConfigurableTree(root, new HashMap<String, Object>());
		
		assertEquals(2, node.getDataFromRoot());
		assertEquals(5, leaf.getDataFromNode());
		
	}
	
}