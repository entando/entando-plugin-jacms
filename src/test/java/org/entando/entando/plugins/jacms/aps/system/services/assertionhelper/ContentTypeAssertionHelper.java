package org.entando.entando.plugins.jacms.aps.system.services.assertionhelper;

import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentDto;
import org.entando.entando.aps.system.services.assertionhelper.ComponentUsageEntityAssertionHelper;
import org.entando.entando.aps.system.services.page.IPageService;
import org.entando.entando.plugins.jacms.aps.system.services.mockhelper.ContentMockHelper;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.component.ComponentUsageEntity;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;

public class ContentTypeAssertionHelper {

    /**
     * does assertions on the received PagedMetadata basing on the default ContentMockHelper mocked data
     * @param usageDetails
     */
    public static void assertUsageDetails(PagedMetadata<ComponentUsageEntity> usageDetails, int totalItems, int currIntems, int page) {
        Assertions.assertEquals(totalItems, usageDetails.getTotalItems());
        Assertions.assertEquals(currIntems, usageDetails.getBody().size());
        Assertions.assertEquals(page, usageDetails.getPage());
        IntStream.range(0, usageDetails.getBody().size())
                .forEach(i -> {
                    ComponentUsageEntity componentUsageEntity = new ComponentUsageEntity(ComponentUsageEntity.TYPE_CONTENT, ContentMockHelper.CONTENT_ID + i, IPageService.STATUS_ONLINE);
                    ComponentUsageEntityAssertionHelper.assertComponentUsageEntity(componentUsageEntity, usageDetails.getBody().get(i));
                });
    }
    
    public static void assertUsageDetails(PagedMetadata<ComponentUsageEntity> usageDetails, ContentDto[] utilizers, int totalItems, int pageNumber) {
        Assertions.assertEquals(totalItems, usageDetails.getTotalItems());
        Assertions.assertEquals(utilizers.length, usageDetails.getBody().size());
        Assertions.assertEquals(pageNumber, usageDetails.getPage());
        List<ComponentUsageEntity> usageEntityList = Arrays.stream(utilizers)
                .map((contentDto) -> new ComponentUsageEntity(ComponentUsageEntity.TYPE_CONTENT,
                        contentDto.getId(),
                        IPageService.STATUS_ONLINE))
                .collect(Collectors.toList());
        IntStream.range(0, usageDetails.getBody().size())
                .forEach((i) -> ComponentUsageEntityAssertionHelper.assertComponentUsageEntity(usageEntityList.get(i), usageDetails.getBody().get(i)));
    }
    
}
