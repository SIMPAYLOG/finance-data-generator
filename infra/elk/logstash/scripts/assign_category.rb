require 'json'

def filter(event)
  # 최초 한 번만 로딩
  if !$category_map
    path = "/usr/share/logstash/config/reverse_mapping.json"
    $category_map = JSON.parse(File.read(path))
  end

  desc = event.get("memo")
    transaction_type = event.get("transactionType")

    # transactionType이 WITHDRAW인 경우만 카테고리 매핑
    if transaction_type == "WITHDRAW"
      if desc && $category_map[desc]
        main_cat, sub_cat = $category_map[desc]
        event.set("category", main_cat)
        event.set("subcategory", sub_cat)
      else
        event.set("category", "otherGoodsServices")
        event.set("subcategory", "miscellaneous")
      end
    else
      # 그 외 타입은 빈 값
      event.set("category", "")
      event.set("subcategory", "")
    end

    return [event]
  end