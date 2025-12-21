package com.furqas.video_service.model

import com.furqas.video_service.model.enum.PlaylistVisibility
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import java.util.UUID

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "playlists")
class Playlist(

    @Id
    val id: UUID,
    val name: String,
    val description: String,

    val thumbnailUrl: String,

    val visibility: PlaylistVisibility,

    // Videos must be ordered
    val videos: List<UUID>,

    ){
}