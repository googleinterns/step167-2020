import React from "react";
import { Map, GoogleApiWrapper, Marker } from "google-maps-react";
import PropTypes from "prop-types";

const RecipeUploadMap = props => {
  const displayMarker = () => {
    if (props.location) {
      return (
        <Marker
          position={{
            lat: props.location.latitude,
            lng: props.location.longitude,
          }}
          onClick={() => {
            props.setLocation(null);
            console.log("Marker clicked.");
          }}
        />
      );
    }
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    const newCenter = {
      lat: clickEvent.latLng.lat(),
      lng: clickEvent.latLng.lng(),
    };
    const newLocation = {
      latitude: newCenter.lat,
      longitude: newCenter.lng,
    };
    props.setLocation(newLocation);
    map.setZoom(5);
    map.panTo(newCenter);
  };

  return (
    <Map google={props.google} zoom={2} style={mapStyles} initialCenter={{ lat: 0, lng: 0 }} onClick={mapClicked}>
      {displayMarker()}
    </Map>
  );
};

RecipeUploadMap.propTypes = {
  location: PropTypes.object,
  setLocation: PropTypes.func,
  google: PropTypes.object,
};

const mapStyles = {
  width: "95%",
  height: "95%",
};

export default GoogleApiWrapper({
  apiKey: "AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc",
})(RecipeUploadMap);
