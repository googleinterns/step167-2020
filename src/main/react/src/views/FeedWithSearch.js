import React, { useState } from "react";
import PropTypes from "prop-types";
import { CCol, CRow } from "@coreui/react";
import TagSelect from "../components/TagSelect";
import Feed from "./Feed";

const FeedWithSearch = props => {
  const [selectedTags, setSelectedTags] = useState({});

  return (
    <>
      <CRow>
        <CCol>
          <TagSelect tags={selectedTags} setTags={setSelectedTags} />
        </CCol>
      </CRow>
      <br></br>
      <br></br>
      <Feed feedType={props.feedType} selectedTags={selectedTags} />
    </>
  );
};

FeedWithSearch.propTypes = {
  feedType: PropTypes.string,
};

export default FeedWithSearch;
