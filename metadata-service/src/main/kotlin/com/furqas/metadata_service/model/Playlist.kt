package com.furqas.metadata_service.model

import com.furqas.metadata_service.model.enum.PlaylistVisibility
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
    var name: String,
    var description: String,

    var thumbnailUrl: String,

    var visibility: PlaylistVisibility,

    // Videos must be ordered
    var videos: MutableList<UUID>,

    ){
}