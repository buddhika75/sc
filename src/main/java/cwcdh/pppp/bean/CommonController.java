/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package cwcdh.pppp.bean;

import cwcdh.pppp.entity.Solution;
import cwcdh.pppp.enums.AreaType;
import cwcdh.pppp.enums.InstitutionType;
import cwcdh.pppp.enums.ItemType;
import cwcdh.pppp.enums.WebUserRole;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import cwcdh.pppp.enums.ComponentSetType;
import cwcdh.pppp.enums.ComponentSex;
import cwcdh.pppp.enums.DataCompletionStrategy;
import cwcdh.pppp.enums.DataModificationStrategy;
import cwcdh.pppp.enums.DataPopulationStrategy;
import cwcdh.pppp.enums.ItemArrangementStrategy;
import cwcdh.pppp.enums.Month;
import cwcdh.pppp.enums.PanelType;
import cwcdh.pppp.enums.Quarter;
import cwcdh.pppp.enums.QueryCriteriaMatchType;
import cwcdh.pppp.enums.QueryDataType;
import cwcdh.pppp.enums.QueryFilterAreaType;
import cwcdh.pppp.enums.QueryFilterPeriodType;
import cwcdh.pppp.enums.QueryLevel;
import cwcdh.pppp.enums.QueryOutputType;
import cwcdh.pppp.enums.QueryType;
import cwcdh.pppp.enums.QueryVariableEvaluationType;
import cwcdh.pppp.enums.RelationshipType;
import cwcdh.pppp.enums.RenderType;
import cwcdh.pppp.enums.SelectionDataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class CommonController implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of HOSecurity
     */
    public CommonController() {
    }

    public Date dateFromString(String dateString, String format) {
        if (format == null || format.trim().equals("")) {
            format = "dd/MM/yyyy";
        }
        SimpleDateFormat formatter1 = new SimpleDateFormat(format);
        try {
            return formatter1.parse(dateString);
        } catch (ParseException ex) {
            return null;
        }
    }

    public Date startOfTheDay() {
        return startOfTheDay(new Date());
    }
    
    public Map<Long, Solution> arrayToMap(List<Solution> lst){
        HashMap<Long, Solution> m = new HashMap<>();
        for(Solution s:lst){
            m.put(s.getId(), s);
        }
        return m;
    }
    
    public List<Solution> commonItems(List<Solution> lst1, List<Solution> lst2){
        HashMap<Long, Solution> m = new HashMap<>();
        for(Solution s1:lst1){
            boolean foundInBoth=false;
            for(Solution s2:lst2){
                if(s1.equals(s2)){
                    foundInBoth = true;
                }
            }
            if(foundInBoth){
                m.put(s1.getId(), s1);
            }
        }
        List<Solution> sols = new ArrayList<>();
        sols.addAll(m.values());
        return sols;
    }
    

    public Date startOfTheDay(Date date) {
        Calendar d = Calendar.getInstance();
        d.setTime(date);
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);
        return d.getTime();
    }

    public Date endOfTheDay() {
        return endOfTheDay(new Date());
    }

    public Date endOfTheDay(Date date) {
        Calendar d = Calendar.getInstance();
        d.setTime(startOfTheDay(date));
        d.add(Calendar.DATE, 1);
        d.add(Calendar.MILLISECOND, -1);
        return d.getTime();
    }

    public String dateToString() {
        return dateToString(Calendar.getInstance().getTime());
    }

    public String dateToString(Date date) {
        return dateToString(date, "dd MMMM yyyy");
    }

    public String dateToString(Date date, String format) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public String encrypt(String word) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.encrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public String hash(String word) {
        try {
            BasicPasswordEncryptor en = new BasicPasswordEncryptor();
            return en.encryptPassword(word);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean matchPassword(String planePassword, String encryptedPassword) {
        BasicPasswordEncryptor en = new BasicPasswordEncryptor();
        return en.checkPassword(planePassword, encryptedPassword);
    }

    public String decrypt(String word) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.decrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public String decrypt(String word, String encryptKey) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.decrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public WebUserRole[] getWebUserRoles() {
        return WebUserRole.values();
    }

    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public AreaType[] getAreaTypes() {
        return AreaType.values();
    }

    public ComponentSetType[] getComponentSetTypes() {
        return ComponentSetType.values();
    }

    public ComponentSex[] getComponentSex() {
        return ComponentSex.values();
    }

    public RenderType[] getRenderTypes() {
        RenderType[] rts = new RenderType[]{
            RenderType.Autocomplete,
            RenderType.Calendar,
            RenderType.Date_Picker,
            RenderType.Input_Text,
            RenderType.Input_Text_Area,
            RenderType.Drop_Down_Menu,
            RenderType.List_Box,
            RenderType.Link,
            RenderType.Boolean_Button,
            RenderType.Boolean_Checkbox,
            };
        return rts;
    }

    public RelationshipType[] getRelationshipTypes() {
        return RelationshipType.values();
    }

    public RelationshipType[] getPopulationTypes() {
        RelationshipType[] ps = new RelationshipType[]{
            RelationshipType.Empanelled_Female_Population,
            RelationshipType.Empanelled_Male_Population,
            RelationshipType.Empanelled_Population,
            RelationshipType.Estimated_Midyear_Female_Population,
            RelationshipType.Estimated_Midyear_Male_Population,
            RelationshipType.Estimated_Midyear_Population,
            RelationshipType.Over_35_Female_Population,
            RelationshipType.Over_35_Male_Population,
            RelationshipType.Over_35_Population,};
        return ps;
    }

    public SelectionDataType[] getSelectionDataTypes() {
        SelectionDataType[] sdts = new SelectionDataType[]{
            SelectionDataType.Short_Text,
            SelectionDataType.Long_Text,
            SelectionDataType.Byte_Array,
            SelectionDataType.Integer_Number,
            SelectionDataType.Real_Number,
            SelectionDataType.Boolean,
            SelectionDataType.DateTime,
            SelectionDataType.Item_Reference,
            SelectionDataType.Area_Reference,
            SelectionDataType.Institution_Reference};
        return sdts;
    }

    public DataPopulationStrategy[] getDataPopulationStrategies() {
        DataPopulationStrategy[] d = new DataPopulationStrategy[]{DataPopulationStrategy.None, DataPopulationStrategy.From_Client_Value, DataPopulationStrategy.From_Last_Encounter, DataPopulationStrategy.From_Last_Encounter_of_same_formset, DataPopulationStrategy.From_Last_Encounter_of_same_clinic};
        return d;
    }

    public DataCompletionStrategy[] getDataCompletionStrategies() {
        return DataCompletionStrategy.values();
    }

    public DataModificationStrategy[] getDataModificationStrategies() {
        return DataModificationStrategy.values();
    }

    public ItemArrangementStrategy[] getItemArrangementStrategies() {
        return ItemArrangementStrategy.values();
    }

    public ItemType[] getItemTypes() {
        return ItemType.values();
    }

    public PanelType[] getPanelTypes() {
        return PanelType.values();
    }

    public static Date startOfTheYear() {
        return startOfTheYear(new Date());
    }

    public static Date startOfTheYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
        return c.getTime();
    }

    public static Double getDoubleValue(String result) {
        Double d = null;
        try {
            d = Double.parseDouble(result);
        } catch (Exception e) {
            d = null;
        }
        return d;
    }

    public static Long getLongValue(String result) {
        Long l = null;
        try {
            l = Long.parseLong(result);
        } catch (Exception e) {
            l = null;
        }
        return l;
    }

    public static Integer getIntegerValue(String result) {
        Integer d = null;
        try {
            d = Integer.parseInt(result);
        } catch (Exception e) {
            d = null;
        }
        return d;//To change body of generated methods, choose Tools | Templates.
    }

    public static Integer getMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.MONTH);
    }

    public static Integer getYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR);
    }

    public static Integer getQuarter(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int month = c.get(Calendar.MONTH);
        return (month / 3) + 1;
    }

    public Month[] getMonths() {
        return Month.values();
    }

    public Quarter[] getQuarters() {
        return Quarter.values();
    }

    public QueryOutputType[] getQueryOutputTypes() {
        return QueryOutputType.values();
    }

    public QueryCriteriaMatchType[] getQueryCriteriaMatchTypes() {
        return QueryCriteriaMatchType.values();
    }

    public QueryType[] getQueryType() {
        return QueryType.values();
    }

    public QueryLevel[] getQueryLevels() {
        return QueryLevel.values();
    }

    public QueryDataType[] getQueryDataTypes() {
        return QueryDataType.values();
    }

    public QueryVariableEvaluationType[] getQueryVariableEvaluationType() {
        return QueryVariableEvaluationType.values();
    }

    public QueryFilterPeriodType[] getQueryFilterPeriodTypes() {
        return QueryFilterPeriodType.values();
    }

    public QueryFilterPeriodType[] getQueryFilterPeriodTypesWithoutYearAndQuarter() {
        QueryFilterPeriodType[] ts = new QueryFilterPeriodType[]{QueryFilterPeriodType.All, QueryFilterPeriodType.Period, QueryFilterPeriodType.After, QueryFilterPeriodType.Before};
        return ts;
    }

    public QueryFilterAreaType[] getQueryFilterAreaType() {
        return QueryFilterAreaType.values();
    }

    public QueryFilterAreaType[] getQueryFilterAreaTypeUpToDistrictLevel() {
        QueryFilterAreaType[] ts = new QueryFilterAreaType[]{QueryFilterAreaType.National, QueryFilterAreaType.Province_List,
            QueryFilterAreaType.District_List, QueryFilterAreaType.Province, QueryFilterAreaType.Distirct, QueryFilterAreaType.Province_District_list};
        return ts;
    }

}
