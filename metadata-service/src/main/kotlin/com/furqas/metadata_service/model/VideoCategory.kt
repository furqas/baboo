package com.furqas.metadata_service.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "video_categories")
class VideoCategory(
    @Id
    val id: Long,
    val name: String,
    val imageUrl: String
) {

}