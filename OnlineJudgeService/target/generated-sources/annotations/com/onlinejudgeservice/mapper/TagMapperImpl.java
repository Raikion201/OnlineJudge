package com.onlinejudgeservice.mapper;

import com.onlinejudgeservice.dto.request.TagRequest;
import com.onlinejudgeservice.dto.response.TagResponse;
import com.onlinejudgeservice.entity.Tag;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-25T16:26:25+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class TagMapperImpl implements TagMapper {

    @Override
    public Tag toEntity(TagRequest request) {
        if ( request == null ) {
            return null;
        }

        Tag.TagBuilder tag = Tag.builder();

        tag.name( request.getName() );
        tag.slug( request.getSlug() );
        tag.description( request.getDescription() );

        return tag.build();
    }

    @Override
    public TagResponse toResponse(Tag tag) {
        if ( tag == null ) {
            return null;
        }

        TagResponse.TagResponseBuilder tagResponse = TagResponse.builder();

        tagResponse.id( tag.getId() );
        tagResponse.name( tag.getName() );
        tagResponse.slug( tag.getSlug() );
        tagResponse.description( tag.getDescription() );
        tagResponse.createdAt( tag.getCreatedAt() );

        return tagResponse.build();
    }

    @Override
    public void updateEntity(TagRequest request, Tag tag) {
        if ( request == null ) {
            return;
        }

        tag.setName( request.getName() );
        tag.setSlug( request.getSlug() );
        tag.setDescription( request.getDescription() );
    }
}
