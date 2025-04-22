package com.springtutorials.generatedocx.service;

import com.springtutorials.generatedocx.dto.ServiceDeliveryReportData;
import com.springtutorials.generatedocx.dto.ServiceDeliveryReportItemData;
import com.springtutorials.generatedocx.util.MoneyAmountConverter;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final String TEMPLATE_PATH = "templates/act_template.docx";

    private final PlaceholderService placeholderService;

    @Value("classpath:templates/act_template.docx")
    private Resource templateResource;
    public byte[] generateDocument() throws Exception {

        InputStream inputStream = templateResource.getInputStream();

        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateInputStream);

        //  WordprocessingMLPackage template = XmlUtils.deepCopy(wordMLPackage); //jakarta.xml.bind.JAXBException: org.docx4j.openpackaging.packages.WordprocessingMLPackage is not known to this context
        WordprocessingMLPackage template = (WordprocessingMLPackage) wordMLPackage.clone();

        MainDocumentPart documentPart = template.getMainDocumentPart();

        VariablePrepare.prepare(template);

        ServiceDeliveryReportData reportData = getReportData();
        Map<String, String> variables = placeholderService.createReplacementsMap(reportData);

        List<Object> tables1 = documentPart.getContent();

        List<Object> tables = documentPart.getContent()
                .stream()
                .filter(obj -> obj instanceof JAXBElement &&
                        ((JAXBElement<?>) obj).getValue() instanceof Tbl)
                .map(obj -> ((JAXBElement<?>) obj).getValue())
                .collect(Collectors.toList());

        if (!tables.isEmpty()) {
            Tbl table = (Tbl) tables.get(0);

            Tr templateRow = (Tr) table.getContent().get(1);
            table.getContent().remove(1);

            if (reportData.getServiceData() != null) {
                for (ServiceDeliveryReportItemData item : reportData.getServiceData()) {
                    Tr newRow = XmlUtils.deepCopy(templateRow);
                    Map<String, String> serviceVariables = placeholderService.createReplacementsMap(item);
                    serviceVariables.forEach((k, v) -> {
                        replaceInRow(newRow, k, v);
                    });
                    table.getContent().add(table.getContent().size() - 1, newRow);
                }
            }
/*            for (int i = 0; i < 10; i++) {
                Tr newRow = XmlUtils.deepCopy(templateRow);

                // Заменяем переменные в строке
                replaceInRow(newRow, "serviceName", "ServiceName" + i);
                replaceInRow(newRow, "value1", formatNumber(BigDecimal.valueOf(i)));
                replaceInRow(newRow, "value2", formatNumber(BigDecimal.valueOf(i)));

                // Вставляем строку перед итоговой
                table.getContent().add(table.getContent().size() - 1, newRow);
            }*/

        }

        documentPart.variableReplace(variables);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        template.save(outputStream);

        return outputStream.toByteArray();
    }

    private void replaceInRow(Tr row, String placeholder, String value) {
        List<Object> texts = getAllElementsFromObject(row, Text.class);
        for (Object obj : texts) {
            Text text = (Text) obj;
            if (text.getValue().contains("${" + placeholder + "}")) {
                text.setValue(text.getValue().replace("${" + placeholder + "}", value));
            }
        }
    }

    private List<Object> getAllElementsFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof JAXBElement)
            obj = ((JAXBElement<?>) obj).getValue();

        if (obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementsFromObject(child, toSearch));
            }
        }
        return result;
    }

    private String formatNumber(BigDecimal number) {
        if (number == null) return "";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(number);
    }

    private ServiceDeliveryReportData getReportData(){
        ServiceDeliveryReportData data = new ServiceDeliveryReportData();
        data.setProfileNumber("3576861-240801-120454");
        data.setReportPeriod("серпень 2024 р");

        data.setBankName("Акціонерне товариство «Державний ощадний банк України» ");
        data.setBankSignerPosition("заступника начальника філії – Чернівецьке обласне управління АТ« Ощадбанк » з роздрібного бізнесу");
        data.setBankSignerName("Шовкопляс Інни Василівни");
        data.setBankSigningReason("довіреності посвідченої 20.09.2023 року приватним нотаріусом Київського нотаріального округу Лосєвим В.В. зареєстрованої в реєстрі за №2060");

        data.setClientName("Товариство з обмеженою відповідальністю «Чернівецька обласна енергопостачальна компанія»");
        data.setClientSignerPosition("директора");
        data.setClientSignerName("Бобзи Ірини Олексіївни");
        data.setClientSigningReason("Статуту");

        data.setPeriodStartDate("01 серпня 2024 р");
        data.setPeriodEndDate("31 серпня 2024 р");


        data.setTotalAmount("246 246,42");
        data.setTotalAmountInWords(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(246246.42)));
        data.setTotalCommissionAmount("12,51");
        data.setTotalCommissionAmountInWords(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(12.51)));

        data.setCommissionAccount("UA603563340000003739490000303");
        data.setCommissionAccountMfo("356334");
        data.setCommissionAccountEdrpoy("09356307");


        data.setFooterBankName("Акціонерне товариство</w:t><w:br/><w:t>" +
                "«Державний ощадний банк України»</w:t><w:br/><w:t>" +
                "Україна, 01023 м. Київ, вул. Госпітальна,12Г</w:t><w:br/><w:t>" +
                "Філія - Чернівецьке обласне управління</w:t><w:br/><w:t>" +
                "АТ «Ощадбанк»</w:t><w:br/><w:t>");
        data.setFooterClientName("Товариство з обмеженою відповідальністю «Чернівецька обласна енергопостачальна компанія»");
        data.setBankDetails("58013, м. Чернівці, вул. Героїв Майдану, 244</w:t><w:br/><w:t>" +
                "IBAN UA603563340000003739490000303</w:t><w:br/><w:t>" +
                "в філії-Чернівецьке обласне управління АТ «Ощадбанк»,</w:t><w:br/><w:t>" +
                "код Банку 356334,</w:t><w:br/><w:t>" +
                "код в ЄДРПОУ 09356307,</w:t><w:br/><w:t>" +
                "ІПН 000321226656,</w:t><w:br/><w:t>" +
                "Номер філії 212,</w:t><w:br/><w:t>" +
                "тел. (0372) 58-55-89; факс 58-55-76</w:t><w:br/><w:t>");
        data.setClientDetails("58005, Чернівецька область, Чернівецький район, місто Чернівці, вулиця Пауля Целана, 6</w:t><w:br/><w:t>" +
                "IBAN UA253563340000026038312576861,</w:t><w:br/><w:t>" +
                "в АТ «Ощадбанк»,</w:t><w:br/><w:t>" +
                "код в ЄДРПОУ 42102122</w:t><w:br/><w:t>" +
                "Тел. (0372) 583385</w:t><w:br/><w:t>" +
                "Ел. адресinfo@ek.cv.ua</w:t><w:br/><w:t>");
        data.setFooterBankSignerPosition("Заступник начальника філії – Чернівецьке обласне управління АТ «Ощадбанк» з роздрібного бізнесу");
        data.setFooterBankSignerName("І.В. Шовкопляс");
        data.setFooterClientSignerPosition("Директор ТОВ «ЧОЕК»");
        data.setFooterClientSignerName("І.О. Бобза");

        List<ServiceDeliveryReportItemData> services = new ArrayList<>();
        services.add(new ServiceDeliveryReportItemData("послуга з постачання електричної енергії (Вижницький ЦОК)",
                "123 123,21","0,1", "123,23"));
        services.add(new ServiceDeliveryReportItemData("послуга з постачання електричної енергії (Чернівецький ЦОК)",
                "321 456,21","0,1", "321,45"));

        data.setServiceData(services);
return data;
    }
}
